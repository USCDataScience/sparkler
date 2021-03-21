package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.storage.solr.SolrGroupPartition
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars
import org.apache.spark.{Partition, SparkContext, TaskContext}
import org.apache.spark.rdd.RDD

/**
  * Created by shah on 6/14/17.
  */

class MemexCrawlDbRDD(sc: SparkContext,
                      job: SparklerJob,
                      sortBy: String = MemexCrawlDbRDD.DEFAULT_ORDER,
                      generateQry: String = MemexCrawlDbRDD.DEFAULT_FILTER_QRY,
                      maxGroups: Int = MemexCrawlDbRDD.DEFAULT_GROUPS,
                      topN: Int = MemexCrawlDbRDD.DEFAULT_TOPN)
  extends RDD[Resource](sc, Seq.empty) {


  assert(topN > 0)
  assert(maxGroups > 0)

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SolrGroupPartition = split.asInstanceOf[SolrGroupPartition]
    val batchSize = 100
    val query = new SolrQuery(generateQry)
    query.addFilterQuery(s"""${Constants.storage.PARENT}:"${escapeQueryChars(partition.group)}"""")
    query.addFilterQuery(s"${Constants.storage.CRAWL_ID}:${job.id}")
    query.set("sort", sortBy)
    query.setRows(batchSize)

    new SolrResultIterator[Resource](job.newStorageProxy().getClient(), query,
      batchSize, classOf[Resource], closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    val qry = new SolrQuery(generateQry)
    qry.addFilterQuery(s"${Constants.storage.CRAWL_ID}:${job.id}")
    qry.set("sort", sortBy)
    qry.set("group", true)
    qry.set("group.ngroups", true)
    qry.set("group.field", Constants.storage.PARENT)
    qry.set("group.limit", 0)
    qry.setRows(maxGroups)
    val proxy = job.newStorageProxy()
    val client = proxy.getClient()
    val groupRes = client.query(qry).getGroupResponse.getValues.get(0)
    val grps = groupRes.getValues
    MemexCrawlDbRDD.LOG.info(s"selecting ${grps.size()} out of ${groupRes.getNGroups}")
    val res = new Array[Partition](grps.size())
    for (i <- 0 until grps.size()) {
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SolrGroupPartition(i, grps.get(i).getGroupValue)
    }
    proxy.close()
    res
  }
}


object MemexCrawlDbRDD extends Loggable {

  val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  val DEFAULT_GROUPS = 1000
  val DEFAULT_TOPN = 1000
}


