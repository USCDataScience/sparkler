package edu.usc.irds.sparkler.pipeline

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import edu.usc.irds.sparkler.base.CliTool
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{Content, CrawlData, Resource, SparklerJob}
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler.{CrawlDbRDD, UnSerializableSolrBeanSink, SolrSink}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.nutch.metadata.Metadata
import org.apache.nutch.protocol
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}
import org.apache.tika.metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.LinkContentHandler
import org.kohsuke.args4j.Option

import scala.collection.JavaConverters._

/**
  *
  * @since 5/28/16
  */
class Crawler extends CliTool {

  import Crawler._

  @Option(name = "-m", aliases = Array("--master"),
    usage = "Spark Master URI. Ignore this if job is started by spark-submit")
  var sparkMaster:String = _

  @Option(name = "-j", aliases = Array("--job"), required = true,
    usage = "Job id. Set to resume job, skip to auto generate")
  var jobId:String = _

  @Option(name = "-o", aliases = Array("--out"),
    usage = "Output path, default is job id")
  var outputPath:String = _

  @Option(name = "-t", aliases = Array("--topN"),
    usage = "Top urls per domain to be selected for a round")
  var topN:Int = 1 << 10

  @Option(name = "-g", aliases = Array("--max-groups"),
    usage = "Max Groups to be selected for fetch..")
  var maxGroups:Int = 1 << 7

  //TODO: URL normalizers
  //TODO: URL filters
  //TODO: Data Sink
  //TODO: Robots.txt
  //TODO: Fetcher + User Agent
  //TODO: JS render
  //TODO: Job Id

  override def run(): Unit ={

    if (this.outputPath == null) {
      this.outputPath = jobId
    }
    val taskId = JobUtil.newSegmentId(true)
    val job = new SparklerJob(jobId, taskId)
    LOG.info(s"Starting the job:$jobId, task:$taskId")

    val conf = new SparkConf().setAppName(jobId)
    if (sparkMaster != null) {
      conf.setMaster(sparkMaster)
    }
    val sc = new SparkContext(conf)
    val fetchDelay = 1000l

    val rdd = new CrawlDbRDD(sc, job, maxGroups = 10, topN=topN)
    val solrc = job.newCrawlDbSolrClient()

    //Local reference hack to serialize these without serializing the whole outer object
    val fetch = FETCH_FUNC
    val outlinksParse = OUTLINKS_PARSE_FUNC
    val statusUpdate = STATUS_UPDATE_FUNC

    //rdd.foreach(r => println(r.id))
    val fetchedRdd = rdd.map(r => (r.group, r))
      .groupByKey()
      .flatMap({ case(grp, rs) => new FairFetcher(rs.iterator, fetchDelay, fetch, outlinksParse)})
      .persist()

    //Step : Update status of fetched resources
    val statusUpdateRdd:RDD[SolrInputDocument] = fetchedRdd.map(d => statusUpdate(d.res, d.content))
    val sinkFunc = new SolrSink(job)
    val res = sc.runJob(statusUpdateRdd, sinkFunc)


    //Step : UPSERT outlinks
    val outlinksRdd = fetchedRdd.flatMap({data => for(u <- data.outLinks) yield (u, data.res)})             //expand the set
      .reduceByKey({case(r1, r2) => if (r1.depth <= r2.depth) r1 else r2})  // pick a parent
      //TODO: url filter
      //TODO: url normalize
      .map({case (link, parent) => new Resource(link, parent.depth + 1, job, NEW)})  //create a new resource

    val outLinksUpsertFunc:((TaskContext, Iterator[Resource]) => Any)= (ctx, docs) => {
        val solrc = job.newCrawlDbSolrClient().crawlDb
        //TODO: handle this in server side - tell solr to skip docs if they already exist
        val newResources:Iterator[Resource] = for (doc <- docs if solrc.getById(doc.id) == null) yield doc
        LOG.info("Inserting new resources to Solr ")
        new UnSerializableSolrBeanSink[Resource](solrc)(ctx, newResources)
        LOG.debug("New resources inserted, Closing..")
        solrc.close()
    }
    sc.runJob(outlinksRdd, outLinksUpsertFunc)

    val outputPath = this.outputPath + "/" + taskId
    LOG.info(s"Storing output at $outputPath")
    fetchedRdd.filter(_.content.status == FETCHED )
      .map(d => (new Text(d.res.url), d.content.toNutchContent(new Configuration())))
      .saveAsHadoopFile[SequenceFileOutputFormat[Text, protocol.Content]](outputPath)

    LOG.info("Shutting down Spark CTX..")
    sc.stop()

    LOG.info("Committing crawldb..")
    solrc.commitCrawlDb()
    solrc.close()
  }
}

trait SerializableFunction[A, B] extends ((A) => B) with Serializable{}
trait SerializableFunction2[A1, A2, R] extends ((A1, A2) => R) with Serializable{}
trait SerializableFunction3[A1, A2, A3, R] extends ((A1, A2, A3) => R) with Serializable{}

class FairFetcher(val resources:Iterator[Resource], val delay:Long,
                  val fetchFunc:(Resource => Content),
                  val linkParseFunc:((Resource, Content) => Set[String]))
  extends Iterator[CrawlData]{

  import Crawler.LOG
  val hitCounter = new AtomicLong()
  var lastHit:String = null

  override def hasNext: Boolean = resources.hasNext

  override def next(): CrawlData = {

    val data = new CrawlData(resources.next())
    val nextFetch = hitCounter.get() + delay
    val waitTime = nextFetch - System.currentTimeMillis()
    if (waitTime > 0){
        LOG.debug("    Waiting for {} ms, {}", waitTime, data.res.url)
        Thread.sleep(waitTime)
    }

    //STEP: Fetch
    data.content = fetchFunc(data.res)
    lastHit = data.res.url
    hitCounter.set(System.currentTimeMillis())

    //STEP: Parse
    data.outLinks = linkParseFunc(data.res, data.content)

    data
  }
}


object Crawler{
  val LOG = org.slf4j.LoggerFactory.getLogger(Crawler.getClass)
  val FETCH_TIMEOUT = 3000

  val FETCH_FUNC = new SerializableFunction[Resource, Content] {
    override def apply(resource: Resource): Content = {

      LOG.info("FETCHING {}", resource.url)
      //FIXME: this is a prototype, make it real
      //TODO: handle errors
      val fetchedAt = new Date()
      val metadata = new Metadata()
      try {
        val urlConn = new URL(resource.url).openConnection()
        urlConn.setConnectTimeout(FETCH_TIMEOUT)

        val inStream = urlConn.getInputStream
        val outStream = new ByteArrayOutputStream()
        Iterator.continually(inStream.read)
          .takeWhile(-1 != )
          .foreach(outStream.write)
        inStream.close()

        val rawData = outStream.toByteArray
        outStream.close()
        val status:ResourceStatus = FETCHED
        val contentType = urlConn.getContentType
        new Content(resource.url, rawData, contentType, rawData.length, Array(),
          fetchedAt, status, metadata)
      } catch {
        case e:Exception => {
          LOG.error(e.getMessage, e)
          new Content(resource.url, null, null, -1, Array(), fetchedAt, ERROR,  metadata)
        }
      }
    }
  }

  val OUTLINKS_PARSE_FUNC = new SerializableFunction2[Resource, Content, Set[String]] {
    override def apply(resource: Resource, content: Content): Set[String] = {
      val stream = new ByteArrayInputStream(content.content)
      val linkHandler = new LinkContentHandler()
      val parser = new AutoDetectParser()
      val meta = new metadata.Metadata()
      meta.set("resourceName", content.url)
      parser.parse(stream, linkHandler, meta)
      stream.close()
      linkHandler.getLinks.asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
    }
  }

  val STATUS_UPDATE_FUNC = new SerializableFunction[(Resource, Content), SolrInputDocument] {
    override def apply(args: (Resource, Content)): SolrInputDocument = {
      val (res, content) = args
      val sUpdate = new SolrInputDocument()
      //FIXME: handle failure case
      //val x:java.util.Map[String, Object] = Map("ss" -> new Object).asJava
      sUpdate.setField(Resource.ID, res.id)
      sUpdate.setField(Resource.STATUS, Map("set" -> content.status.toString).asJava)
      sUpdate.setField(Resource.LAST_FETCHED_AT, Map("set" -> content.fetchedAt).asJava)
      sUpdate.setField(Resource.LAST_UPDATED_AT, Map("set" -> new Date()).asJava)
      sUpdate.setField(Resource.NUM_TRIES, Map("inc" -> 1).asJava)
      sUpdate.setField(Resource.NUM_FETCHES, Map("inc" -> 1).asJava)
      sUpdate
    }
  }

  def main(args: Array[String]) {

    val argss = "-j sparkler-job-1464744090100 -m local[*]".split(" ")
    for (_ <- 1 to 1){
      new Crawler().run(argss)
    }
  }
}
