package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.storage.{StorageRDD, SparklerGroupPartition}
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
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.aggregations.Aggregation

/**
  * @since 4/3/21
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
    var searchRequest : SearchRequest = new SearchRequest()
    var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    var q : QueryBuilder = QueryBuilders.boolQuery()
      .filter(QueryBuilders.termQuery(Constants.storage.STATUS, ResourceStatus.UNFETCHED))
      .filter(QueryBuilders.termQuery(Constants.storage.CRAWL_ID, job.id))
    println(Constants.storage.STATUS + " => " + ResourceStatus.UNFETCHED)
    println(Constants.storage.CRAWL_ID + " => " + job.id)

    searchSourceBuilder.sort(Constants.storage.DISCOVER_DEPTH, SortOrder.ASC)
    searchSourceBuilder.sort(Constants.storage.SCORE, SortOrder.DESC)

    var groupBy : TermsAggregationBuilder = AggregationBuilders.terms("by" + Constants.storage.PARENT)
                                                          .field(Constants.storage.PARENT + ".keyword")
    groupBy.size(1)
    searchSourceBuilder.aggregation(groupBy)
    searchSourceBuilder.size(maxGroups)

    searchSourceBuilder.query(q)
    searchRequest.source(searchSourceBuilder)

    val proxy = job.newStorageProxy()
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    var searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
    println(searchResponse.toString())

    var aggregations : Aggregations = searchResponse.getAggregations()
    if (aggregations == null) println("Aggregations is NULL")
    else println("Aggregations is NOT NULL")

    var aggregationList = aggregations.asList()
    println(aggregationList.size())

    var aggregation : Aggregation = aggregations.get("by" + Constants.storage.PARENT)
    if (aggregation == null) println("Aggregation is NULL")
    else {
      println("Aggregation is NOT NULL: " + aggregation.getName())
      println("Type: " + aggregation.getType())
    }



    val res = new Array[Partition](1)

    proxy.close()
    res
  }
}

object ElasticsearchRDD extends StorageRDD {

  override val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 1000
  override val DEFAULT_TOPN = 1000
}
