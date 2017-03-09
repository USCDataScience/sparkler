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


import edu.usc.irds.sparkler.{Constants, CrawlDbRDD, SparklerConfiguration}
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{ResourceStatus, CrawlData, Resource, SparklerJob}
import edu.usc.irds.sparkler.solr.{SolrStatusUpdate, SolrUpsert}
import edu.usc.irds.sparkler.util.JobUtil
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.nutch.protocol
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.StringArrayOptionHandler

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
    usage = "Crawdb URI.")
  var sparkSolr: String = sparklerConf.get(Constants.key.CRAWLDB).asInstanceOf[String]

  @Option(name = "-id", aliases = Array("--id"), required = true,
    usage = "Job id. When not sure, get the job id from injector command")
  var jobId: String = ""

  @Option(name = "-o", aliases = Array("--out"),
    usage = "Output path, default is job id")
  var outputPath: String = ""

  @Option(name = "-ke", aliases = Array("--kafka-enable"),
    usage = "Enable Kafka, default is false i.e. disabled")
  var kafkaEnable: Boolean = sparklerConf.get(Constants.key.KAFKA_ENABLE).asInstanceOf[Boolean]

  @Option(name = "-kls", aliases = Array("--kafka-listeners"),
    usage = "Kafka Listeners, default is localhost:9092")
  var kafkaListeners: String = sparklerConf.get(Constants.key.KAFKA_LISTENERS).asInstanceOf[String]

  @Option(name = "-ktp", aliases = Array("--kafka-topic"),
    usage = "Kafka Topic, default is sparkler")
  var kafkaTopic: String = sparklerConf.get(Constants.key.KAFKA_TOPIC).asInstanceOf[String]

  @Option(name = "-tn", aliases = Array("--top-n"),
    usage = "Top urls per domain to be selected for a round")
  var topN: Int = sparklerConf.get(Constants.key.GENERATE_TOPN).asInstanceOf[Int]

  @Option(name = "-tg", aliases = Array("--top-groups"),
    usage = "Max Groups to be selected for fetch..")
  var topG: Int = sparklerConf.get(Constants.key.GENERATE_TOP_GROUPS).asInstanceOf[Int]

  @Option(name = "-i", aliases = Array("--iterations"),
    usage = "Number of iterations to run")
  var iterations: Int = 1

  @Option(name = "-fd", aliases = Array("--fetch-delay"),
    usage = "Delay between two fetch requests")
  var fetchDelay: Long = sparklerConf.get(Constants.key.FETCHER_SERVER_DELAY).asInstanceOf[Number].longValue()

  @Option(name = "-aj", handler = classOf[StringArrayOptionHandler], aliases = Array("--add-jars"), usage = "Add sparkler jar to spark context")
  var jarPath : Array[String] = new Array[String](0)

  var job: SparklerJob = _
  var sc: SparkContext = _

  def init(): Unit = {
    if (this.outputPath.isEmpty) {
      this.outputPath = jobId
    }
    val conf = new SparkConf().setAppName(jobId)
    if (!sparkMaster.isEmpty) {
      conf.setMaster(sparkMaster)
    }
    if (!sparkSolr.isEmpty){
      sparklerConf.asInstanceOf[java.util.HashMap[String,String]].put("crawldb.uri", sparkSolr)
    }

    sc = new SparkContext(conf)

    if(!jarPath.isEmpty && jarPath(0) == "true"){
      sc.getConf.setJars(Array[String](getClass.getProtectionDomain.getCodeSource.getLocation.getPath))
    }
    else if(!jarPath.isEmpty) {
      sc.getConf.setJars(jarPath)
    }

    job = new SparklerJob(jobId, sparklerConf, "")
    FetchFunction.init(job)

  }
  //TODO: URL nor malizers
  //TODO: Robots.txt

  override def run(): Unit = {

    //STEP : Initialize environment
    init()

    val solrc = this.job.newCrawlDbSolrClient()
    val localFetchDelay = fetchDelay
    val job = this.job // local variable to bypass serialization

    for (_ <- 1 to iterations) {
      val taskId = JobUtil.newSegmentId(true)
      job.currentTask = taskId
      LOG.info(s"Starting the job:$jobId, task:$taskId")


      val rdd = new CrawlDbRDD(sc, job, maxGroups = topG, topN = topN)
      val fetchedRdd = rdd.map(r => (r.getGroup, r))
        .groupByKey()
        .flatMap({ case (grp, rs) => new FairFetcher(job, rs.iterator, localFetchDelay,
          FetchFunction, ParseFunction, OutLinkFilterFunction) })
        .persist()

      if (kafkaEnable) {
        storeContentKafka(kafkaListeners, kafkaTopic.format(jobId), fetchedRdd)
      }

      //Step: Update status of fetched resources
      val statusUpdateRdd: RDD[SolrInputDocument] = fetchedRdd.map(d => StatusUpdateSolrTransformer(d))
      val statusUpdateFunc = new SolrStatusUpdate(job)
      sc.runJob(statusUpdateRdd, statusUpdateFunc)

      //Step: Filter Outlinks and Upsert new URLs into CrawlDb
      val outlinksRdd = OutLinkUpsert(job, fetchedRdd)
      val upsertFunc = new SolrUpsert(job)
      sc.runJob(outlinksRdd, upsertFunc)

      //Step: Store these to nutch segments
      val outputPath = this.outputPath + "/" + taskId

      storeContent(outputPath, fetchedRdd)

      LOG.info("Committing crawldb..")
      solrc.commitCrawlDb()
    }
    solrc.close()
    //PluginService.shutdown(job)
    LOG.info("Shutting down Spark CTX..")
    sc.stop()
  }
}

object OutLinkUpsert extends ((SparklerJob, RDD[CrawlData]) => RDD[Resource]) with Serializable with Loggable {
  override def apply(job: SparklerJob, rdd: RDD[CrawlData]): RDD[Resource] = {

    //Step : UPSERT outlinks
    rdd.flatMap({ data => for (u <- data.parsedData.outlinks) yield (u, data.fetchedData.getResource) }) //expand the set

      .reduceByKey({ case (r1, r2) => if (r1.getDiscoverDepth <= r2.getDiscoverDepth) r1 else r2 }) // pick a parent
      //TODO: url normalize
      .map({ case (link, parent) => new Resource(link, parent.getDiscoverDepth + 1, job, UNFETCHED,
      parent.getFetchTimestamp, parent.getId) }) //create a new resource
  }
}

object Crawler extends Loggable with Serializable{

  def storeContent(outputPath:String, rdd:RDD[CrawlData]): Unit = {
    LOG.info(s"Storing output at $outputPath")
    rdd.filter(_.fetchedData.getResource.getStatus.equals(ResourceStatus.FETCHED.toString))
      .map(d => (new Text(d.fetchedData.getResource.getUrl), d.fetchedData.toNutchContent(new Configuration())))
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
    new Crawler().run(args)
  }
}
