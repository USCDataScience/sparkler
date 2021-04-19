package edu.usc.irds.sparkler.pipeline

import com.fasterxml.jackson.databind.cfg.ConfigOverride
import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import org.apache.commons.validator.routines.UrlValidator
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import edu.usc.irds.sparkler.pipeline._
import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import edu.usc.irds.sparkler.model.ResourceStatus.UNFETCHED
import edu.usc.irds.sparkler.model.{CrawlData, Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.storage.{StorageProxyFactory, StatusUpdate, ScoreUpdateTransformer, StatusUpdateTransformer}
import java.io.File
import scala.collection.mutable
import scala.io.Source
import edu.usc.irds.sparkler.util.JobUtil
import org.apache.spark.rdd.RDD


class CrawlerRunner {

  import Crawler.LOG

  val urlValidator: UrlValidator = new UrlValidator()

  val conf: SparklerConfiguration = Constants.defaults.newDefaultConfig()

  var outputPath: String = ""

  var sc: SparkContext = _

  // scalastyle:off parameter.number
  def runCrawler(configOverride: Array[Any], sparkStorage: String, jobId: String, fetchDelay: Long, iterations: Int,
                 deepCrawlHostFile: File, deepCrawlHostnames: Array[String], sc: SparkContext, topN: Int, topG: Int,
                 job: SparklerJob, kafkaEnable: Boolean, kafkaListeners: String, kafkaTopic: String,
                 sparklerConf: SparklerConfiguration, sparkMaster:String, op: String, jarPath :Array[String]): String = {
    //STEP : Initialize environment
    this.outputPath = op
    val job = init(configOverride, jobId, sparkStorage, databricksEnable = false, sparklerConf, outputPath, sparkMaster, jarPath)
    val storageFactory = job.getStorageFactory()

    val storageProxy = storageFactory.getProxy()
    LOG.info("Committing crawldb..")
    storageProxy.commitCrawlDb()
    val localFetchDelay = fetchDelay
    for (_ <- 1 to iterations) {
      var deepCrawlHosts: mutable.Set[String] = new mutable.HashSet[String]()
      if(deepCrawlHostFile != null) {
        if(deepCrawlHostFile.isFile) {
          deepCrawlHosts ++= Source.fromFile(deepCrawlHostFile).getLines().toSet
        }
      }
      else if (deepCrawlHostnames.length > 0) {
        deepCrawlHosts ++= deepCrawlHostnames.toSet
      }
      if (deepCrawlHosts.size > 0) {
        LOG.info(s"Deep crawling hosts ${deepCrawlHosts.toString}")
        var taskId = JobUtil.newSegmentId(true)
        job.currentTask = taskId
        val deepRdd = storageFactory.getDeepRDD(this.sc, job, maxGroups = topG, topN = topN,
          deepCrawlHosts = deepCrawlHostnames)
        val fetchedRdd = deepRdd.map(r => (r.getGroup, r))
          .groupByKey()
          .flatMap({ case (grp, rs) => new FairFetcher(job, rs.iterator, localFetchDelay,
            FetchFunction, ParseFunction, OutLinkFilterFunction, StatusUpdateTransformer)
          })
          .persist()


        if (kafkaEnable) {
          Crawler.storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), fetchedRdd)
        }

        val scoredRdd = score(fetchedRdd, job)
        //Step: Store these to nutch segments
        this.outputPath = this.outputPath + "/" + taskId

        Crawler.storeContent(outputPath, scoredRdd)

        LOG.info("Committing crawldb..")
        storageProxy.commitCrawlDb()
      }

      var taskId = JobUtil.newSegmentId(true)
      job.currentTask = taskId
      LOG.info(s"Starting the job:$jobId, task:$taskId")

      val rdd = storageFactory.getRDD(this.sc, job, maxGroups = topG, topN = topN)
      val fetchedRdd = rdd.map(r => (r.getGroup, r))
        .groupByKey()
        .flatMap({ case (grp, rs) => new FairFetcher(job, rs.iterator, localFetchDelay,
          FetchFunction, ParseFunction, OutLinkFilterFunction, StatusUpdateTransformer) })
        .persist()

      if (kafkaEnable) {
        Crawler.storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), fetchedRdd)
      }
      val scoredRdd = score(fetchedRdd, job)
      //Step: Store these to nutch segments
      this.outputPath = this.outputPath + "/" + taskId

      Crawler.storeContent(this.outputPath, scoredRdd)

      LOG.info("Committing crawldb..")
      storageProxy.commitCrawlDb()
    }
    storageProxy.close()
    //PluginService.shutdown(job)
    LOG.info("Shutting down Spark CTX..")
    this.sc.stop()

    this.outputPath
  }

  def score(fetchedRdd: RDD[CrawlData], job: SparklerJob): RDD[CrawlData] = {
    val joba = job.asInstanceOf[SparklerJob]
    val storageFactory = joba.getStorageFactory()

    val scoredRdd = fetchedRdd.map(d => ScoreFunction(joba, d))

    val scoreUpdateRdd: RDD[Map[String, Object]] = scoredRdd.map(d => ScoreUpdateTransformer(d))
    val scoreUpdateFunc = new StatusUpdate(joba)
    this.sc.runJob(scoreUpdateRdd, scoreUpdateFunc)

    //TODO (was OutlinkUpsert)
    val outlinksRdd = scoredRdd.flatMap({ data => for (u <- data.parsedData.outlinks)
      yield (u, data.fetchedData.getResource) }) //expand the set
      .reduceByKey({ case (r1, r2) => if (r1.getDiscoverDepth <= r2.getDiscoverDepth) r1 else r2 }) // pick a parent
      //TODO: url normalize
      .map({ case (link, parent) => new Resource(link, parent.getDiscoverDepth + 1, joba, UNFETCHED,
        parent.getFetchTimestamp, parent.getId, parent.getScoreAsMap) })
    val upsertFunc = storageFactory.getUpserter(joba)
    this.sc.runJob(outlinksRdd, upsertFunc)

    scoredRdd
  }
  // scalastyle:off
  def init(configOverride: Array[Any], jobId: String, sparkStorage: String, databricksEnable: Boolean,
           sparklerConf: SparklerConfiguration, outputPath: String, sparkMaster: String,
           jarPath: Array[String]): SparklerJob = {

    if (configOverride != null && !configOverride.isEmpty){
      sparklerConf.overloadConfig(configOverride.mkString(" "));
    }
    if (this.outputPath.isEmpty) {
      this.outputPath = jobId
    }
    val conf = new SparkConf().setAppName(jobId)
    if (!sparkMaster.isEmpty) {
      conf.setMaster(sparkMaster)
    }
    if (sparkStorage != null && !sparkStorage.isEmpty){
      val dbToUse: String = sparklerConf.get(Constants.key.CRAWLDB_BACKEND).asInstanceOf[String]
      sparklerConf.asInstanceOf[java.util.HashMap[String,String]].put(dbToUse+".uri", sparkStorage)
    }

    if (databricksEnable) {
      LOG.info("Databricks spark is enabled")
      this.sc = SparkSession.builder().master("local").getOrCreate().sparkContext
    }
    else {
      this.sc = new SparkContext(conf)
    }

    if(!jarPath.isEmpty && jarPath(0) == "true"){
      this.sc.getConf.setJars(Array[String](getClass.getProtectionDomain.getCodeSource.getLocation.getPath))
    }
    else if(!jarPath.isEmpty) {
      this.sc.getConf.setJars(jarPath)
    }

    new SparklerJob(jobId, sparklerConf, "")
  }
  // scalastyle:oon
}
