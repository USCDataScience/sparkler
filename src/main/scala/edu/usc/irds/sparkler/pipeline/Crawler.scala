package edu.usc.irds.sparkler.pipeline

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{Content, CrawlData, Resource}
import edu.usc.irds.sparkler.{CrawlDbRDD, SolrBeanSink, SolrSink}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.LinkContentHandler

import scala.collection.JavaConverters._

/**
  *
  * @since 5/28/16
  */
class Crawler {

  import Crawler._

  def run(): Unit ={
    val conf = new SparkConf().setAppName(Crawler.getClass.getName).setMaster("local[*]")
    val sc = new SparkContext(conf)
    val solr= "http://localhost:8983/solr/crawldb"
    val fetchDelay = 5 * 1000l

    val rdd = new CrawlDbRDD(sc, solr)

    //Local reference hack to serialize these without serializing the whole outer object
    val fetch = FETCH_FUNC
    val outlinksParse = OUTLINKS__PARSE_FUNC
    val statusUpdate = STATUS_UPDATE_FUNC

    //rdd.foreach(r => println(r.id))
    val fetchedRdd = rdd.map(r => (r.group, r))
      .groupByKey()
      .flatMap({ case(grp, rs) => new FairFetcher(rs.iterator, fetchDelay, fetch, outlinksParse)})
      .persist()

    //Step : Update status of fetched resources
    val statusUpdateRdd:RDD[SolrInputDocument] = fetchedRdd.map(d => statusUpdate(d.res, d.content))
    val sinkFunc = new SolrSink(solr)
    val res = sc.runJob(statusUpdateRdd, sinkFunc)


    //Step : UPSERT outlinks
    val outlinksRdd = fetchedRdd.flatMap({data => for(u <- data.outLinks) yield (u, data.res)})             //expand the set
      .reduceByKey({case(r1, r2) => if (r1.depth <= r2.depth) r1 else r2})  // pick a parent
      //TODO: url filter
      //TODO: url normalize
      .map({case (link, parent) => new Resource(link, parent.depth + 1, NEW)})  //create a new resource

    val outLinksUpsertFunc:((TaskContext, Iterator[Resource]) => Any)= (ctx, docs) => {
        val solrc = new HttpSolrClient(solr)
        //TODO: handle this in server side - tell solr to skip docs if they already exist
        val newResources:Iterator[Resource] = for (doc <- docs if solrc.getById(doc.id) == null) yield doc
        LOG.info("Inserting new resources to Solr ")
        new SolrBeanSink(solrc)(ctx, newResources)
        LOG.debug("New resources inserted, Closing..")
        solrc.close()
    }
    sc.runJob(outlinksRdd, outLinksUpsertFunc)

    LOG.info("Shutting down Spark CTX..")
    sc.stop()

    val solrc = new HttpSolrClient(solr)
    solrc.commit()
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
        LOG.debug("Waiting for {}ms, res={}", waitTime, data.res.id)
        LOG.debug("Group's Last Hit = {}", lastHit)
        Thread.sleep(waitTime)
    }

    //STEP: Fetch
    data.content = fetchFunc(data.res)
    lastHit = data.res.id
    hitCounter.set(System.currentTimeMillis())

    //STEP: Parse
    data.outLinks = linkParseFunc(data.res, data.content)

    data
  }
}


object Crawler{
  val LOG = org.slf4j.LoggerFactory.getLogger(Crawler.getClass)

  val FETCH_FUNC = new SerializableFunction[Resource, Content] {
    override def apply(resource: Resource): Content = {
      LOG.info("Fetching resource:{}", resource.id)
      //FIXME: this is a prototype, make it real
      //TODO: handle errors
      val urlConn = new URL(resource.id).openConnection()

      val inStream =urlConn.getInputStream
      val outStream = new ByteArrayOutputStream()
      Iterator.continually(inStream.read)
        .takeWhile(-1 != )
        .foreach(outStream.write)
      inStream.close()

      val rawData = outStream.toByteArray
      outStream.close()
      val fetchedAt = new Date()
      val status:ResourceStatus = FETCHED
      val contentType = urlConn.getContentType
      new Content(resource.id, rawData, contentType, rawData.length, Array(),
        fetchedAt, status, null)
    }
  }

  val OUTLINKS__PARSE_FUNC = new SerializableFunction2[Resource, Content, Set[String]] {
    override def apply(resource: Resource, content: Content): Set[String] = {
      val stream = new ByteArrayInputStream(content.content)
      val linkHandler = new LinkContentHandler()
      val parser = new AutoDetectParser()
      val meta = new Metadata()
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
    for(i <- 0 to 0){
      println(s"===Round $i")
      val t = System.currentTimeMillis()
      new Crawler().run()
      println(s"${System.currentTimeMillis() - t}")
      Thread.sleep(5000)
    }
    /*
    val r = new Resource("https://twitter.com/", 0, NEW)
    val c = FETCH_FUNC(r)
    val ls = OUTLINKS__PARSE_FUNC(r, c)
    for (elem <- ls) {
      println(elem)
    }*/

  }
}
