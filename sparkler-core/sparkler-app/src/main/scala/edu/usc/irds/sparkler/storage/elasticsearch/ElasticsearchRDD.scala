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
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.aggregations.Aggregation
import org.apache.lucene.queryparser.classic.QueryParserBase

import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit
import org.elasticsearch.common.document.DocumentField

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

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SparklerGroupPartition = split.asInstanceOf[SparklerGroupPartition]
    val batchSize = 100

    var searchRequest : SearchRequest = new SearchRequest("crawldb")
    var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    var q : BoolQueryBuilder = QueryBuilders.boolQuery()
      .filter(QueryBuilders.termQuery(Constants.storage.PARENT, QueryParserBase.escape(partition.group)))
      .filter(QueryBuilders.termQuery(Constants.storage.CRAWL_ID, job.id))

    // querying
    for (query <- generateQry.split(",")) {
      try {
        val Array(field, value) = query.split(":").take(2)
        q.filter(QueryBuilders.termQuery(field, value))
      } catch {
        case e: Exception => println("Exception parsing generateQry: " + generateQry)
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
        case e: Exception => println("Exception parsing sortBy: " + sortBy)
      }
    }

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

    new ElasticsearchResultIterator[Resource](client, searchRequest,
      batchSize, classOf[Resource], closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    var searchRequest : SearchRequest = new SearchRequest("crawldb")
    var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    // querying
    var q : BoolQueryBuilder = QueryBuilders.boolQuery()
      .filter(QueryBuilders.termQuery(Constants.storage.CRAWL_ID, job.id))

//    for (query <- generateQry.split(",")) {
//      try {
//        val Array(field, value) = query.split(":").take(2)
//        q.filter(QueryBuilders.termQuery(field, value)) // <-- this doesn't work for status/UNFETCHED??
//        println(field + " => " + value)
//      } catch {
//        case e: Exception => println("Exception parsing generateQry: " + generateQry)
//      }
//    }

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
        case e: Exception => println("Exception parsing sortBy: " + sortBy)
      }
    }

    // grouping
    var groupBy : TermsAggregationBuilder = AggregationBuilders.terms("by" + Constants.storage.PARENT)
                                                          .field(Constants.storage.PARENT + ".keyword")
    groupBy.size(1)
    searchSourceBuilder.aggregation(groupBy)
    searchSourceBuilder.size(maxGroups)

    searchRequest.source(searchSourceBuilder)

    val proxy = job.newStorageProxy()
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    var searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
//    println(searchResponse.toString())

//    var aggregations : Aggregations = searchResponse.getAggregations()
//    if (aggregations == null) println("Aggregations is NULL")
//    else println("Aggregations is NOT NULL")
//
//    var aggregationList = aggregations.asList()
//    println(aggregationList.size())
//
//    var aggregation : Aggregation = aggregations.get("by" + Constants.storage.PARENT)
//    if (aggregation == null) println("Aggregation is NULL")
//    else {
//      println("Aggregation is NOT NULL: " + aggregation.getName())
//      println("Type: " + aggregation.getType())
//    }

    var shs : SearchHits = searchResponse.getHits()
//    println("searchhits size: " + shs.getTotalHits().value)

//    shs.getHits().foreach(sh => {
//      println("SearchHit - source: " + sh.getSourceAsString())
//      var source: java.util.Map[java.lang.String, java.lang.Object] = sh.getSourceAsMap()
//      println("SearchHit - source - url: " + source.get("url"))
//    })

    val res = new Array[Partition](shs.getTotalHits().value.toInt)
    for (i <- 0 until shs.getTotalHits().value.toInt) {
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SparklerGroupPartition(i, shs.getHits()(i).getSourceAsMap().get("group").asInstanceOf[String])
//      println(shs.getHits()(i).getSourceAsMap().get("group").asInstanceOf[String])
    }

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
