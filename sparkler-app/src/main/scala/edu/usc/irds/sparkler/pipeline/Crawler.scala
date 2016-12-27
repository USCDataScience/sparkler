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

import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.service.PluginService
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler.{Constants, CrawlDbRDD, SparklerConfiguration, URLFilter}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.kohsuke.args4j.Option

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

  @Option(name = "-aj", aliases = Array("--add-jar"), usage = "Add sparkler jar to spark context")
  var path: String = ""

  var job: SparklerJob = _
  var sc: SparkContext = _

  def init(): Unit = {
    if (this.outputPath.isEmpty) {
      this.outputPath = jobId
    }

    //http://techkites.blogspot.fr/
    val conf = new SparkConf().setAppName(jobId)
      //.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    if (!sparkMaster.isEmpty) {
      conf.setMaster(sparkMaster)
    }
    sc = new SparkContext(conf)

    if (!path.isEmpty && path == "true") {
      sc.addJar(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    }
    else if (!path.isEmpty) {
      sc.addJar(path)
    }

    job = new SparklerJob(jobId, sparklerConf, "")
  }

  override def run(): Unit = {

    //Initialize environment
    init()

    val solrc = this.job.solrClient()
    val job = this.job

    for (_ <- 1 to iterations) {
      job.currentTask = JobUtil.newSegmentId(true)
      LOG.info(s"Starting job $jobId")

      // Crawl new web documents
      val fetchedRdd = new CrawlDbRDD(sc, job, maxGroups = topG, topN = topN)
        .mapPartitions(it => FetchFunction.fetch(job, it))
        .persist()

      //Update status
      fetchedRdd
        .map(d => StatusUpdateSolrTransformer(d))
        .foreachPartition(p => job.solrClient().addResourceDocs(p).close())

      //Filter and insert new URLs
      addOutlinks(job, fetchedRdd)

      LOG.info("Committing.")
      solrc.commit()
      fetchedRdd.unpersist()
    }

    solrc.close()
    LOG.info("Shutdown.")
    sc.stop()
  }
}

object Crawler extends Loggable with Serializable {
  /**
    * Used to send crawl dumps to the Kafka Messaging System.
    * There is a sparklerProducer instantiated per partition and
    * used to send all crawl data in a partition to Kafka.
    *
    * @param listeners list of listeners example : host1:9092,host2:9093,host3:9094
    * @param topic     the kafka topic to use
    * @param rdd       the input RDD consisting of the FetchedData
    */
  def storeContentKafka(listeners: String, topic: String, rdd: RDD[FetchedData]): Unit = {
    rdd.foreachPartition(crawlData_iter => {
      val sparklerProducer = new SparklerProducer(listeners, topic)
      crawlData_iter.foreach(crawlData => {
        sparklerProducer.send(crawlData.content)
      })
    })
  }

  def addOutlinks(job: SparklerJob, rdd: RDD[FetchedData]): Unit = {
    rdd.flatMap({ data => for (u <- data.outlinks) yield (u, data.resource) }) //expand the set
      .reduceByKey({ case (r1, r2) => if (r1.depth <= r2.depth) r1 else r2 }) // pick a parent
      .filter({ case (url, parent) => PluginService.getExtension(classOf[URLFilter], job) match {
          case Some(urLFilter) => urLFilter.filter(url, parent.url)
          case None => true
        }
      })
      .map({ case (link, parent) => new Resource(link, parent.depth + 1, job, NEW) }) //create a new resource
      .foreachPartition(p => job.solrClient().addResources(p).close());
  }

  def main(args: Array[String]): Unit = {
    new Crawler().run(args)
  }
}
