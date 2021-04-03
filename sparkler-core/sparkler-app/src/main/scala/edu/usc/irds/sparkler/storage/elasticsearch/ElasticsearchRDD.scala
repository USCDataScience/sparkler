package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.storage.StorageRDD
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.client.RestHighLevelClient

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
    var searchRequest : SearchRequest = new SearchRequest();
    var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder();

    var q : QueryBuilder = QueryBuilders.boolQuery()
      .filter(QueryBuilders.termQuery(Constants.storage.STATUS, ResourceStatus.UNFETCHED))
      .filter(QueryBuilders.termQuery(Constants.storage.CRAWL_ID, job.id))

    searchSourceBuilder.query(q);
    searchRequest.source(searchSourceBuilder);


    val proxy = job.newStorageProxy()
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    var searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

    ???
  }
}

object ElasticsearchRDD extends StorageRDD {

  override val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 1000
  override val DEFAULT_TOPN = 1000
}
