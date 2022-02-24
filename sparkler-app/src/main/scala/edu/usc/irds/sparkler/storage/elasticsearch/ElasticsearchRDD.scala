package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.storage.{SparklerGroupPartition, StorageProxyFactory, StorageRDD}
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.{ParsedTerms, TermsAggregationBuilder}
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.aggregations.Aggregation
import org.apache.lucene.queryparser.classic.QueryParserBase
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit
import org.elasticsearch.common.document.DocumentField
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram

import scala.collection.JavaConversions._

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

  val storageFactory: StorageProxyFactory = job.getStorageFactory

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SparklerGroupPartition = split.asInstanceOf[SparklerGroupPartition]
    val batchSize = 100

    val searchRequest : SearchRequest = new SearchRequest("crawldb")
    val searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    val q : BoolQueryBuilder = QueryBuilders.boolQuery()
      .must(QueryBuilders.matchQuery(Constants.storage.PARENT, QueryParserBase.escape(partition.group)))
      .must(QueryBuilders.matchQuery(Constants.storage.CRAWL_ID, job.id))

    // querying
    for (query <- generateQry.split(",")) {
      try {
        val Array(field, value) = query.split(":").take(2)
        q.must(QueryBuilders.matchQuery(field, value))
      } catch {
        case _: Exception => println("Exception parsing generateQry: " + generateQry)
      }
    }

    // sorting
    for (sort <- sortBy.split(",")) {
      try {
        val Array(field, order) = sort.split(" ").take(2)
        if (order.toLowerCase() == "asc") {
          searchSourceBuilder.sort(field, SortOrder.ASC)
        }
        else if (order.toLowerCase() == "desc") {
          searchSourceBuilder.sort(field, SortOrder.DESC)
        }
        else {
          println("Invalid sort order for: " + field)
        }
      } catch {
        case _: Exception => println("Exception parsing sortBy: " + sortBy)
      }
    }

    searchSourceBuilder.size(batchSize)

    searchSourceBuilder.query(q)
    searchRequest.source(searchSourceBuilder)

    val proxy = storageFactory.getProxy
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case _: ClassCastException => println("client is not RestHighLevelClient.")
    }

    new ElasticsearchResultIterator[Resource](client, searchRequest,
      batchSize, classOf[Resource], closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    val searchRequest : SearchRequest = new SearchRequest("crawldb")
    val searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    // querying
    val q : BoolQueryBuilder = QueryBuilders.boolQuery()
      .must(QueryBuilders.matchQuery(Constants.storage.CRAWL_ID, job.id))

    for (query <- generateQry.split(",")) {
      try {
        val Array(field, value) = query.split(":").take(2)
        q.must(QueryBuilders.matchQuery(field, value))
      } catch {
        case _: Exception => println("Exception parsing generateQry: " + generateQry)
      }
    }

    searchSourceBuilder.query(q)

    // sorting
    for (sort <- sortBy.split(",")) {
      try {
        val Array(field, order) = sort.split(" ").take(2)
        if (order.toLowerCase() == "asc") {
          searchSourceBuilder.sort(field, SortOrder.ASC)
        }
        else if (order.toLowerCase() == "desc") {
          searchSourceBuilder.sort(field, SortOrder.DESC)
        }
        else {
          println("Invalid sort order for: " + field)
        }
      } catch {
        case _: Exception => println("Exception parsing sortBy: " + sortBy)
      }
    }

    // grouping
    val groupBy : TermsAggregationBuilder = AggregationBuilders.terms("by" + Constants.storage.PARENT)
                                                          .field(Constants.storage.PARENT + ".keyword")
    groupBy.size(1)
    searchSourceBuilder.aggregation(groupBy)
    searchSourceBuilder.size(maxGroups)

    searchRequest.source(searchSourceBuilder)

    val proxy = storageFactory.getProxy
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case _: ClassCastException => println("client is not RestHighLevelClient.")
    }

    val searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
    val aggmap = searchResponse.getAggregations.getAsMap
    val agg2 = aggmap.head._2.asInstanceOf[ParsedTerms]
    val res = new Array[Partition](agg2.getBuckets.size())

    var i = 0
    agg2.getBuckets.foreach(b => {
      res(i) = new SparklerGroupPartition(i, b.getKeyAsString)
      i = i + 1
    })

    proxy.close()
    res
  }
}

object ElasticsearchRDD extends StorageRDD {

  override val DEFAULT_ORDER: String = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY: String = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 1000
  override val DEFAULT_TOPN = 1000
}
