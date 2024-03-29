package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.model.{CrawlData, Resource, SparklerJob}
import edu.usc.irds.sparkler.storage.StorageProxyFactory
import org.apache.spark.rdd.RDD

@SerialVersionUID(100L)
class RunCrawl extends Serializable{
  var i = 0
  def mapCrawl(x: Iterator[(String, Iterable[Resource])], job: SparklerJob): Iterator[CrawlData] = {
    val m = 1000
    val storageFactory: StorageProxyFactory= job.getStorageFactory
    x.flatMap({case (grp, rs) => new FairFetcher(job, rs.iterator, m,
      FetchFunction, ParseFunction, OutLinkFilterFunction, storageFactory.getStatusUpdateTransformer)})
  }

  def runCrawl(f: RDD[(String, Iterable[Resource])], job: SparklerJob): RDD[CrawlData] = {
    f.mapPartitions( x => mapCrawl(x, job))

  }

  def maplogic(r: Resource): (String, Resource) = {
    i = i + 1
    (r.getId, r)
  }

  def map(rdd: RDD[Resource]): RDD[(String, Iterable[Resource])] = {
    rdd.map(r => maplogic(r))
      .groupByKey()
  }
}
