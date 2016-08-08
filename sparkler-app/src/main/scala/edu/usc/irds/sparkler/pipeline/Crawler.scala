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

import edu.usc.irds.sparkler.{Constants, CrawlDbRDD, URLFilter}
import Constants.key
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{CrawlData, Resource, SparklerJob}
import edu.usc.irds.sparkler.service.PluginService
import edu.usc.irds.sparkler.solr.{SolrStatusUpdate, SolrUpsert}
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler.{Constants, CrawlDbRDD, URLFilter}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.nutch.protocol
import org.apache.solr.common.SolrInputDocument
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
  val sparklerConf: Configuration = Constants.defaults.newDefaultConfig()

  @Option(name = "-m", aliases = Array("--master"),
    usage = "Spark Master URI. Ignore this if job is started by spark-submit")
  var sparkMaster: String = sparklerConf.get(key.SPARK_MASTER)

  @Option(name = "-id", aliases = Array("--id"), required = true,
    usage = "Job id. When not sure, get the job id from injector command")
  var jobId: String = ""

  @Option(name = "-o", aliases = Array("--out"),
    usage = "Output path, default is job id")
  var outputPath: String = ""

  @Option(name = "-tn", aliases = Array("--top-n"),
    usage = "Top urls per domain to be selected for a round")
  var topN: Int = sparklerConf.getInt(key.GENERATE_TOPN, DEFAULT_TOP_N)

  @Option(name = "-tg", aliases = Array("--top-groups"),
    usage = "Max Groups to be selected for fetch..")
  var topG: Int = sparklerConf.getInt(key.GENERATE_TOP_GROUPS, DEFAULT_TOP_GROUPS)

  @Option(name = "-i", aliases = Array("--iterations"),
    usage = "Number of iterations to run")
  var iterations: Int = 1

  @Option(name = "-fd", aliases = Array("--fetch-delay"),
    usage = "Delay between two fetch requests")
  var fetchDelay: Long = sparklerConf.getLong(key.FETCHER_SERVER_DELAY, DEFAULT_FETCH_DELAY)

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
    sc = new SparkContext(conf)
    job = new SparklerJob(jobId, sparklerConf, "")
  }
  //TODO: URL normalizers
  //TODO: URL filters
  //TODO: Data Sink
  //TODO: Robots.txt
  //TODO: Fetcher + User Agent
  //TODO: JS render
  //TODO: Job Id

  override def run(): Unit = {

    //STEP : Initialize environment
    init()

    val solrc = this.job.newCrawlDbSolrClient()
    val fetchDelay = DEFAULT_FETCH_DELAY
    val job = this.job // local variable to bypass serialization
    for (_ <- 1 to iterations) {
      val taskId = JobUtil.newSegmentId(true)
      job.currentTask = taskId
      LOG.info(s"Starting the job:$jobId, task:$taskId")

      val rdd = new CrawlDbRDD(sc, job, maxGroups = topG, topN = topN)
      val fetchedRdd = rdd.map(r => (r.group, r))
        .groupByKey()
        .flatMap({ case (grp, rs) => new FairFetcher(rs.iterator, fetchDelay, FetchFunction, ParseFunction) })
        .persist()

      //Step: Update status of fetched resources
      val statusUpdateRdd: RDD[SolrInputDocument] = fetchedRdd.map(d => StatusUpdateSolrTransformer(d))
      val statusUpdateFunc = new SolrStatusUpdate(job)
      sc.runJob(statusUpdateRdd, statusUpdateFunc)

      //Step: Filter Outlinks and Upsert new URLs into CrawlDb
      val outlinksRdd = OutLinkFilterFunc(job, fetchedRdd)
      val upsertFunc = new SolrUpsert(job)
      sc.runJob(outlinksRdd, upsertFunc)

      //Step: Store these to nutch segments
      val outputPath = this.outputPath + "/" + taskId

      storeContent(outputPath, fetchedRdd)

      LOG.info("Committing crawldb..")
      solrc.commitCrawlDb()
    }
    solrc.close()
    LOG.info("Shutting down Spark CTX..")
    sc.stop()
  }
}

object OutLinkFilterFunc extends ((SparklerJob, RDD[CrawlData]) => RDD[Resource]) with Serializable with Loggable {
  override def apply(job: SparklerJob, rdd: RDD[CrawlData]): RDD[Resource] = {



    //Step : UPSERT outlinks
    rdd.flatMap({ data => for (u <- data.outLinks) yield (u, data.res) }) //expand the set

      .reduceByKey({ case (r1, r2) => if (r1.depth <= r2.depth) r1 else r2 }) // pick a parent

      .filter({case (url, parent) =>
        val outLinkFilter:scala.Option[URLFilter] = PluginService.getExtension(classOf[URLFilter], job)
        val result = outLinkFilter.get.filter(url, parent.url)
        LOG.debug(s"$result :: filter(${parent.url} --> $url)")
        result
      })
      //TODO: url normalize
      .map({ case (link, parent) => new Resource(link, parent.depth + 1, job, NEW) }) //create a new resource
  }
}

object Crawler extends Loggable with Serializable{

  val DEFAULT_TOP_N = 1024
  val DEFAULT_TOP_GROUPS = 256
  val DEFAULT_FETCH_DELAY = 1000L

  def storeContent(outputPath:String, rdd:RDD[CrawlData]): Unit = {
    LOG.info(s"Storing output at $outputPath")
    rdd.filter(_.content.status == FETCHED)
      .map(d => (new Text(d.res.url), d.content.toNutchContent(new Configuration())))
      .saveAsHadoopFile[SequenceFileOutputFormat[Text, protocol.Content]](outputPath)
  }

  def main(args: Array[String]): Unit = {
    new Crawler().run(args)
  }
}
