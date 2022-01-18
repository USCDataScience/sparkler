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

  val storageFactory = job.getStorageFactory

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SparklerGroupPartition = split.asInstanceOf[SparklerGroupPartition]
    val batchSize = 100

    var searchRequest : SearchRequest = new SearchRequest("crawldb")
    var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()

    var q : BoolQueryBuilder = QueryBuilders.boolQuery()
      .must(QueryBuilders.matchQuery(Constants.storage.PARENT, QueryParserBase.escape(partition.group)))
      .must(QueryBuilders.matchQuery(Constants.storage.CRAWL_ID, job.id))

    for(url <- deepCrawlHosts) {
      try {
        val hostname = new URL(url).getHost
        // should is similar to OR for Elasticsearch
        q.should(QueryBuilders.matchQuery(Constants.storage.HOSTNAME, hostname))
      } catch {
        case e: Exception => print(s"Exception occured while getting host from $url")
      }
    }

    // querying
    for (query <- generateQry.split(",")) {
      try {
        val Array(field, value) = query.split(":").take(2)
        q.must(QueryBuilders.matchQuery(field, value))
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

    searchSourceBuilder.size(batchSize)

    searchSourceBuilder.query(q)
    searchRequest.source(searchSourceBuilder)

    val proxy = storageFactory.getProxy
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
      .must(QueryBuilders.matchQuery(Constants.storage.CRAWL_ID, job.id))

    for (query <- generateQry.split(",")) {
      try {
        val Array(field, value) = query.split(":").take(2)
        q.must(QueryBuilders.matchQuery(field, value))
      } catch {
        case e: Exception => println("Exception parsing generateQry: " + generateQry)
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

    val proxy = storageFactory.getProxy
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    var searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
    var shs : SearchHits = searchResponse.getHits()
    val res = new Array[Partition](shs.getTotalHits().value.toInt)
    for (i <- 0 until shs.getTotalHits().value.toInt) {
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SparklerGroupPartition(i, shs.getHits()(i).getSourceAsMap().get("group").asInstanceOf[String])
    }

    proxy.close()
    res
  }
}

object ElasticsearchDeepRDD extends StorageRDD {

  override val DEFAULT_ORDER = Constants.storage.DISCOVER_DEPTH + " asc," + Constants.storage.SCORE + " desc"
  override val DEFAULT_FILTER_QRY = Constants.storage.STATUS + ":" + ResourceStatus.UNFETCHED
  override val DEFAULT_GROUPS = 10
  override val DEFAULT_TOPN = 1000
}