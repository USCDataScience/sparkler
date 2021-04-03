package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.storage.StorageRDD
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

import java.net.URL

class ElasticsearchDeepRDD(sc: SparkContext,
                           job: SparklerJob,
                           sortBy: String = ElasticsearchDeepRDD.DEFAULT_ORDER,
                           generateQry: String = ElasticsearchDeepRDD.DEFAULT_FILTER_QRY,
                           maxGroups: Int = ElasticsearchDeepRDD.DEFAULT_GROUPS,
                           topN: Int = ElasticsearchDeepRDD.DEFAULT_TOPN,
                           deepCrawlHosts: Array[String] = new Array[String](0))
  extends RDD[Resource](sc, Seq.empty) {


  assert(topN > 0)
  assert(maxGroups > 0)

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    ???
  }

  override protected def getPartitions: Array[Partition] = {
    ???
  }
}

object ElasticsearchDeepRDD extends StorageRDD {

  override val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 10
  override val DEFAULT_TOPN = 1000
}