package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.model.{CrawlData, Resource, SparklerJob}
import edu.usc.irds.sparkler.storage.solr.StatusUpdateSolrTransformer
import org.apache.spark.rdd.RDD

@SerialVersionUID(100L)
class RunCrawl extends Serializable{
  def mapCrawl(x: Iterator[(String, Iterable[Resource])], job: SparklerJob): Iterator[CrawlData] = {
    val m = 1000
    x.flatMap({case (grp, rs) => new FairFetcher(job, rs.iterator, m,
      FetchFunction, ParseFunction, OutLinkFilterFunction, StatusUpdateSolrTransformer)})
  }

  def runCrawl(f: RDD[(String, Iterable[Resource])], job: SparklerJob): RDD[CrawlData] = {
    f.mapPartitions( x => mapCrawl(x, job))

  }

}