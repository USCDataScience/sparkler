package edu.usc.irds.sparkler.storage.solr

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.storage.{StorageRDD, SparklerGroupPartition}
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

import java.lang.ClassCastException

/**
  * Created by shah on 6/14/17.
  */

class SolrRDD(sc: SparkContext,
              job: SparklerJob,
              sortBy: String = SolrRDD.DEFAULT_ORDER,
              generateQry: String = SolrRDD.DEFAULT_FILTER_QRY,
              maxGroups: Int = SolrRDD.DEFAULT_GROUPS,
              topN: Int = SolrRDD.DEFAULT_TOPN)
  extends RDD[Resource](sc, Seq.empty) {


  assert(topN > 0)
  assert(maxGroups > 0)

  val storageFactory = job.getStorageFactory()

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SparklerGroupPartition = split.asInstanceOf[SparklerGroupPartition]
    val batchSize = 100
    val query = new SolrQuery(generateQry)
    query.addFilterQuery(s"""${Constants.storage.PARENT}:"${escapeQueryChars(partition.group)}"""")
    query.addFilterQuery(s"${Constants.storage.CRAWL_ID}:${job.id}")
    query.set("sort", sortBy)
    query.setRows(batchSize)

    val proxy = storageFactory.getProxy()
    var client : SolrClient = null
    try {
      client = proxy.getClient().asInstanceOf[SolrClient]
    } catch {
      case e: ClassCastException => println("client is not SolrClient.")
    }

    new SolrResultIterator[Resource](client, query,
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
    val proxy = storageFactory.getProxy()

    var client : SolrClient = null
    try {
       client = proxy.getClient().asInstanceOf[SolrClient]
    } catch {
      case e: ClassCastException => println("client is not SolrClient.")
    }

    val groupRes = client.query(qry).getGroupResponse.getValues.get(0)
    val grps = groupRes.getValues
    SolrRDD.LOG.info(s"selecting ${grps.size()} out of ${groupRes.getNGroups}")
    val res = new Array[Partition](grps.size())
    for (i <- 0 until grps.size()) {
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SparklerGroupPartition(i, grps.get(i).getGroupValue)
    }
    proxy.close()
    res
  }
}


object SolrRDD extends StorageRDD {

  override val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 1000
  override val DEFAULT_TOPN = 1000
}


