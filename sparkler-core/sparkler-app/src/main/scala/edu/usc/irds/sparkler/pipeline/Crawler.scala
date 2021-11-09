/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{CrawlData, Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.storage.StorageProxy
import edu.usc.irds.sparkler.storage.solr.{ScoreUpdateSolrTransformer, SolrStatusUpdate, SolrUpsert, StatusUpdateSolrTransformer}
import edu.usc.irds.sparkler.util.{JobUtil, NutchBridge}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.nutch.protocol
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.StringArrayOptionHandler

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.collection.mutable
import scala.io.Source

/**
  *
  * @since 5/28/16
  */
class Crawler extends CliTool {

  import Crawler._

  // Load Sparkler Configuration
  val sparklerConf: SparklerConfiguration = Constants.defaults.newDefaultConfig()

  @Option(name = "-m", aliases = Array("--master"),
    usage = "Spark Master URI. Ignore this if job is started by spark-submit")
  var sparkMaster: String = sparklerConf.get(Constants.key.SPARK_MASTER).asInstanceOf[String]

  @Option(name = "-cdb", aliases = Array("--crawldb"),
    usage = "Crawl DB URI.")
  var sparkStorage: String = sparklerConf.getDatabaseURI()

  @Option(name = "-id", aliases = Array("--id"), required = true,
    usage = "Job id. When not sure, get the job id from injector command")
  var jobId: String = ""

  @Option(name = "-o", aliases = Array("--out"),
    usage = "Output path, default is job id")
  var outputPath: String = "crawl-segments"

  @Option(name = "-ke", aliases = Array("--kafka-enable"),
    usage = "Enable Kafka, default is false i.e. disabled")
  var kafkaEnable: Boolean = sparklerConf.get(Constants.key.KAFKA_ENABLE).asInstanceOf[Boolean]

  @Option(name = "-kls", aliases = Array("--kafka-listeners"),
    usage = "Kafka Listeners, default is localhost:9092")
  var kafkaListeners: String = sparklerConf.get(Constants.key.KAFKA_LISTENERS).asInstanceOf[String]

  @Option(name = "-ktp", aliases = Array("--kafka-topic"),
    usage = "Kafka Topic, default is sparkler")
  var kafkaTopic: String = sparklerConf.get(Constants.key.KAFKA_TOPIC).asInstanceOf[String]

  @Option(name = "-de", aliases = Array("--databricks-enable"),
    usage = "Enable Databricks, default is false i.e. disabled")
  var databricksEnable: Boolean = sparklerConf.get(Constants.key.DATABRICKS_ENABLE).asInstanceOf[Boolean]

  @Option(name = "-tn", aliases = Array("--top-n"),
    usage = "Top urls per domain to be selected for a round")
  var topN: Int = sparklerConf.get(Constants.key.GENERATE_TOPN).asInstanceOf[Int]

  @Option(name = "-tg", aliases = Array("--top-groups"),
    usage = "Max Groups to be selected for fetch..")
  var topG: Int = sparklerConf.get(Constants.key.GENERATE_TOP_GROUPS).asInstanceOf[Int]

  @Option(name = "-i", aliases = Array("--iterations"),
    usage = "Number of iterations to run. Special Feature: Set any number <= 0, eg: -1, to crawl all URLs")
  var iterations: Int = 1

  @Option(name = "-fd", aliases = Array("--fetch-delay"),
    usage = "Delay between two fetch requests")
  var fetchDelay: Long = sparklerConf.get(Constants.key.FETCHER_SERVER_DELAY).asInstanceOf[Number].longValue()

  @Option(name = "-aj", handler = classOf[StringArrayOptionHandler], aliases = Array("--add-jars"), usage = "Add sparkler jar to spark context")
  var jarPath : Array[String] = new Array[String](0)

  @Option(name = "-dc", handler = classOf[StringArrayOptionHandler], forbids = Array("-dcf"),
    aliases = Array("--deep-crawl"), usage = "Deep crawl the provided hosts")
  var deepCrawlHostnames : Array[String] = new Array[String](0)

  @Option(name = "-dcf", forbids = Array("-dc"),
    aliases = Array("--deepcrawl-file"), usage = "Deep crawl the provided hosts in the line separated file")
  var deepCrawlHostFile : File = _


  @Option(name = "-co", aliases = Array("--config-override"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverride: Array[Any] = Array()

  @Option(name = "-co64", aliases = Array("--config-override-encoded"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverrideEncoded: String = ""

  @Option(name = "-cof", aliases = Array("--config-override-file"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverrideFile: String = ""

  @Option(name = "-dq", aliases = Array("--default-query"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var defaultquery: String = ""

  /* Generator options, currently not exposed via the CLI
     and only accessible through the config yaml file
   */
  var sortBy: String = sparklerConf.get(Constants.key.GENERATE_SORTBY).asInstanceOf[String]
  var groupBy: String = sparklerConf.get(Constants.key.GENERATE_GROUPBY).asInstanceOf[String]

  var job: SparklerJob = _
  var sc: SparkContext = _

  def setConfig(): Unit ={
    if (configOverride != ""){
      sparklerConf.overloadConfig(configOverride.mkString(" "));
    }
    if(configOverrideEncoded != ""){
      val decoded = Base64.getDecoder().decode(configOverrideEncoded)
      val str = new String(decoded, StandardCharsets.UTF_8)
      sparklerConf.overloadConfig(str)
    }
    if(configOverrideFile!= ""){
      val fileContents = Source.fromFile(configOverrideFile).getLines.mkString
      sparklerConf.overloadConfig(fileContents)
    }
  }
  def init(): Unit = {
    setConfig()
    if (this.outputPath.isEmpty) {
      this.outputPath = jobId
    }
    val conf = new SparkConf().setAppName(jobId)
    if (sparkMaster != null && sparkMaster.nonEmpty) {
      conf.setMaster(sparkMaster)
    }
    if (sparkStorage.nonEmpty){
      sparklerConf.asInstanceOf[java.util.HashMap[String,String]].put("crawldb.uri", sparkStorage)
    }

    if (databricksEnable) {
      LOG.info("Databricks spark is enabled")
      sc = SparkSession.builder().master("local").getOrCreate().sparkContext
    }
    else {
      sc = new SparkContext(conf)
    }

    if(!jarPath.isEmpty && jarPath(0) == "true"){
      sc.getConf.setJars(Array[String](getClass.getProtectionDomain.getCodeSource.getLocation.getPath))
    }
    else if(!jarPath.isEmpty) {
      sc.getConf.setJars(jarPath)
    }
    import java.nio.file.Files
    val tempDir: File = Files.createTempDirectory("checkpoints").toFile
    sc.setCheckpointDir(tempDir.getPath)

    LOG.info("Setting local job: " + sparklerConf.get("fetcher.headers"))
    job = new SparklerJob(jobId, sparklerConf, "")
  }
  //TODO: URL normalizers
  //TODO: Robots.txt

  override def run(): Unit = {

    //STEP : Initialize environment
    init()

    val storageProxy = this.job.newStorageProxy()
    LOG.info("Committing crawldb..")
    storageProxy.commitCrawlDb()
    val localFetchDelay = fetchDelay
    val job = this.job // local variable to bypass serialization
    GenericFunction(job, GenericProcess.Event.STARTUP,new SQLContext(sc).sparkSession, null)

    for (_ <- 1 to iterations) {
      var deepCrawlHosts = new mutable.HashSet[String]()
      if(deepCrawlHostFile != null) {
        if(deepCrawlHostFile.isFile) {
          deepCrawlHosts ++= Source.fromFile(deepCrawlHostFile).getLines().toSet
        }
      }
      else if (deepCrawlHostnames.length > 0) {
        deepCrawlHosts ++= deepCrawlHostnames.toSet
      }
      if (deepCrawlHosts.nonEmpty) {
        deepCrawl(deepCrawlHosts.toString(), localFetchDelay, storageProxy)
      }

      val taskId = JobUtil.newSegmentId(true)
      job.currentTask = taskId
      LOG.info(s"Starting the job:$jobId, task:$taskId")
      val rc = new RunCrawl

      val rdd = if(defaultquery != "" && defaultquery.nonEmpty){
        new MemexCrawlDbRDD(sc, job, generateQry= defaultquery, maxGroups = topG, topN = topN)
      } else{
        new MemexCrawlDbRDD(sc, job, maxGroups = topG, topN = topN)
      }

      //TODO RESTORE THIS HACK
      val f = rc.map(rdd)
      /*val f = rdd.map(r => (r.getDedupeId, r))
        .groupByKey()*/

      var fetchedRdd: RDD[CrawlData] = null
      var rep: Int = sparklerConf.get("crawl.repartition").asInstanceOf[Number].intValue()
      if(rep <= 0){
        rep = 1
      }
      println("Number of partitions configured: " + rep)
      fetchedRdd = f.repartition(rep).flatMap({ case (grp, rs) => new FairFetcher(job, rs.iterator, localFetchDelay,
        FetchFunction, ParseFunction, OutLinkFilterFunction, StatusUpdateSolrTransformer).toSeq
      }).persist()
      GenericFunction(job, GenericProcess.Event.ITERATION_COMPLETE,new SQLContext(sc).sparkSession, fetchedRdd)
      scoreAndStore(fetchedRdd, taskId, storageProxy)
    }
    storageProxy.close()
    //PluginService.shutdown(job)
    GenericFunction(job, GenericProcess.Event.SHUTDOWN,new SQLContext(sc).sparkSession, null)
    LOG.info("Shutting down Spark CTX..")
    sc.stop()
  }

  def scoreAndStore(fetchedRdd: RDD[CrawlData], taskId: String, storageProxy: StorageProxy): Unit ={
    if (kafkaEnable) {
      storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), fetchedRdd)
    }
    var rep: Int = sparklerConf.get("crawl.repartition").asInstanceOf[Number].intValue()
    if(rep <= 0){
      rep = 1
    }
    val scoredRdd = score(fetchedRdd).repartition(rep)
    //Step: Store these to nutch segments
    val outputPath = this.outputPath + "/" + taskId

    storeContent(outputPath, scoredRdd)

    LOG.info("Committing crawldb..")
    storageProxy.commitCrawlDb()
  }

  def deepCrawl(deepCrawlHosts: String, localFetchDelay: Long, storageProxy: StorageProxy): Unit ={
    LOG.info(s"Deep crawling hosts ${deepCrawlHosts}")
    var taskId = JobUtil.newSegmentId(true)
    job.currentTask = taskId
    val deepRdd = new MemexDeepCrawlDbRDD(sc, job, maxGroups = topG, topN = topN,
      deepCrawlHosts = deepCrawlHostnames)
    val fetchedRdd = deepRdd.map(r => (r.getGroup, r))
      .groupByKey()
      .flatMap({ case (grp, rs) => new FairFetcher(job, rs.iterator, localFetchDelay,
        FetchFunction, ParseFunction, OutLinkFilterFunction, StatusUpdateSolrTransformer)
      })
      .persist()


    if (kafkaEnable) {
      storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), fetchedRdd)
    }

    val scoredRdd = score(fetchedRdd)
    //Step: Store these to nutch segments
    val outputPath = this.outputPath + "/" + taskId

    storeContent(outputPath, scoredRdd)

    LOG.info("Committing crawldb..")
    storageProxy.commitCrawlDb()
  }

  def score(fetchedRdd: RDD[CrawlData]): RDD[CrawlData] = {
    val job = this.job.asInstanceOf[SparklerJob]

    val scoredRdd = fetchedRdd.map(d => ScoreFunction(job, d))

    val scoreUpdateRdd: RDD[SolrInputDocument] = scoredRdd.map(d => ScoreUpdateSolrTransformer(d))
    val scoreUpdateFunc = new SolrStatusUpdate(job)
    scoreUpdateRdd.checkpoint()
    sc.runJob(scoreUpdateRdd, scoreUpdateFunc)

    //TODO (was OutlinkUpsert)
    val outlinksRdd = scoredRdd.flatMap({ data => for (u <- data.parsedData.outlinks) yield (u, data.fetchedData.getResource) }) //expand the set
      .reduceByKey({ case (r1, r2) => if (r1.getDiscoverDepth <= r2.getDiscoverDepth) r1 else r2 }) // pick a parent
      //TODO: url normalize
      .map({ case (link, parent) => new Resource(link, parent.getDiscoverDepth + 1, job, UNFETCHED,
        parent.getFetchTimestamp, parent.getId, parent.getScoreAsMap) })
    val upsertFunc = new SolrUpsert(job)
    sc.runJob(outlinksRdd, upsertFunc)

    scoredRdd
  }


  def processFetched(rdd: RDD[CrawlData]): RDD[CrawlData] = {
    if (kafkaEnable) {
      storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), rdd)
    }

    val job = this.job
    //Step: Index all new URLS
    sc.runJob(OutLinkUpsert(job, rdd), new SolrUpsert(job))

    //Step: Update status+score of fetched resources
    val statusUpdateRdd: RDD[SolrInputDocument] = rdd.map(d => StatusUpdateSolrTransformer(d))
    sc.runJob(statusUpdateRdd, new SolrStatusUpdate(job))

    //Step: Store these to nutch segments
    val outputPath = this.outputPath + "/" + job.currentTask
    //Step : write to FS
    storeContent(outputPath, rdd)

    rdd
  }
}

object OutLinkUpsert extends ((SparklerJob, RDD[CrawlData]) => RDD[Resource]) with Serializable with Loggable {
  override def apply(job: SparklerJob, rdd: RDD[CrawlData]): RDD[Resource] = {

    //Step : UPSERT outlinks
    rdd.flatMap({ data => for (u <- data.parsedData.outlinks) yield (u, data.fetchedData.getResource) }) //expand the set

      .reduceByKey({ case (r1, r2) => if (r1.getDiscoverDepth <= r2.getDiscoverDepth) r1 else r2 }) // pick a parent
      //TODO: url normalize
      .map({ case (link, parent) => new Resource(link, parent.getDiscoverDepth + 1, job, UNFETCHED,
        parent.getFetchTimestamp, parent.getId, parent.getScore) }) //create a new resource
  }
}

object Crawler extends Loggable with Serializable{

  def storeContent(outputPath:String, rdd:RDD[CrawlData]): Unit = {
    LOG.info(s"Storing output at $outputPath")

    object ConfigHolder extends Serializable {
      var config:Configuration = new Configuration()
    }

    rdd.filter(r => r.fetchedData.getResource.getStatus.equals(ResourceStatus.FETCHED.toString))
      .map(d => (new Text(d.fetchedData.getResource.getUrl),
        NutchBridge.toNutchContent(d.fetchedData, ConfigHolder.config)))
      .saveAsHadoopFile[SequenceFileOutputFormat[Text, protocol.Content]](outputPath)
  }

  /**
    * Used to send crawl dumps to the Kafka Messaging System.
    * There is a sparklerProducer instantiated per partition and
    * used to send all crawl data in a partition to Kafka.
    *
    * @param listeners list of listeners example : host1:9092,host2:9093,host3:9094
    * @param topic the kafka topic to use
    * @param rdd the input RDD consisting of the CrawlData
    */
  def storeContentKafka(listeners: String, topic: String, rdd:RDD[CrawlData]): Unit = {
    rdd.foreachPartition(crawlData_iter => {
      val sparklerProducer = new SparklerProducer(listeners, topic)
      crawlData_iter.foreach(crawlData => {
        sparklerProducer.send(crawlData.fetchedData.getContent)
      })
    })
  }

  def main(args: Array[String]): Unit = {
    setLogLevel()
    new Crawler().run(args)
  }
}
