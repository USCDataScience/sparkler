package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

/**
  * Created by shah on 6/14/17.
  */

class ElasticsearchRDD(sc: SparkContext,
                       job: SparklerJob,
                       sortBy: String = ElasticsearchRDD.DEFAULT_ORDER,
                       generateQry: String = ElasticsearchRDD.DEFAULT_FILTER_QRY,
                       maxGroups: Int = ElasticsearchRDD.DEFAULT_GROUPS,
                       topN: Int = ElasticsearchRDD.DEFAULT_TOPN)
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

object ElasticsearchRDD extends Loggable {

  val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  val DEFAULT_GROUPS = 1000
  val DEFAULT_TOPN = 1000
}
