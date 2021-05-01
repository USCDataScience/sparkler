/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.usc.irds.sparkler.storage.elasticsearch

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.apache.spark.TaskContext
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import edu.usc.irds.sparkler.storage.Upserter

import scala.collection.JavaConversions._


/**
 * Created by karanjeets on 6/11/16
 */
class ElasticsearchUpsert(job: SparklerJob) extends ((TaskContext, Iterator[Resource]) => Any) with Serializable with Upserter {

  import edu.usc.irds.sparkler.storage.elasticsearch.ElasticsearchUpsert.LOG

  override def apply(context: TaskContext, docs: Iterator[Resource]): Any = {
    LOG.debug("ElasticsearchUpsert - Inserting new resources into CrawlDb")
    val proxy = job.getStorageFactory().getProxy()
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    //This filter function returns true if there is no other resource  with the same dedupe_id
    val newLinksFilter: (Resource => Boolean) = doc => {
      var searchRequest : SearchRequest = new SearchRequest("crawldb")
      var searchSourceBuilder : SearchSourceBuilder = new SearchSourceBuilder()
      var qry : BoolQueryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.matchQuery(Constants.storage.DEDUPE_ID, doc.getDedupeId))
      searchSourceBuilder.query(qry)
      searchRequest.source(searchSourceBuilder)
      var searchResponse : SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
      searchResponse.getHits().getTotalHits().value.toInt == 0
      // if zero hits, then there are no duplicates
    }
    val newResources = docs.withFilter(newLinksFilter)

    LOG.info("Inserting new resources to Elasticsearch ")
    proxy.addResources(newResources)
    LOG.debug("New resources inserted, Closing..")
    proxy.close()
  }
}

object ElasticsearchUpsert {
  val LOG = LoggerFactory.getLogger(ElasticsearchUpsert.getClass())
}
