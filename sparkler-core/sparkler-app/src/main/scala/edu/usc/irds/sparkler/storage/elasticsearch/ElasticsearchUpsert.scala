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
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.apache.spark.TaskContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


/**
 * Created by karanjeets on 6/11/16
 */
class ElasticsearchUpsert(job: SparklerJob) extends ((TaskContext, Iterator[Resource]) => Any) with Serializable {

  import edu.usc.irds.sparkler.storage.elasticsearch.ElasticsearchUpsert.LOG

  override def apply(context: TaskContext, docs: Iterator[Resource]): Any = {
    LOG.debug("Inserting new resources into CrawlDb")
    val proxy = job.newStorageProxy()
    var client : RestHighLevelClient = null
    try {
      client = proxy.getClient().asInstanceOf[RestHighLevelClient]
    } catch {
      case e: ClassCastException => println("client is not RestHighLevelClient.")
    }

    //TODO: handle this in server side - tell solr to skip docs if they already exist

    //This filter function returns true if there is no other resource  with the same dedupe_id
    val newLinksFilter: (Resource => Boolean) = doc => {
      var qry : QueryBuilder = QueryBuilders.boolQuery()
        .filter(QueryBuilders.termQuery(Constants.storage.DEDUPE_ID, doc.getDedupeId))
      // TODO, find out what the solrupsert logic was checking here and how to implement it
      // with elasticsearch
      // if zero hits, then there are no duplicates
    }
    val newResources = docs.withFilter(newLinksFilter)

    LOG.info("Inserting new resources to Solr ")
    proxy.addResources(newResources)
    LOG.debug("New resources inserted, Closing..")
    proxy.close()
  }
}

object ElasticsearchUpsert {
  val LOG = LoggerFactory.getLogger(ElasticsearchUpsert.getClass())
}
