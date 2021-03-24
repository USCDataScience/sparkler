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

import java.io.{Closeable, File}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.storage.StorageProxy
import edu.usc.irds.sparkler._

import org.apache.http.HttpHost
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

/**
  *
  * @since 3/6/21
  */
class ElasticsearchProxy(var config: SparklerConfiguration) extends StorageProxy with Closeable with Loggable {

  // creates the solr client
  private var crawlDb = newClient(config.getDatabaseURI())

  def newClient(crawlDbUri: String): RestHighLevelClient = {
    val hostname : String = crawlDbUri.substring(0, crawlDbUri.indexOf(':'))
    val port : Int = Integer.valueOf(crawlDbUri.substring(crawlDbUri.indexOf(':') + 1))

    if (crawlDbUri.startsWith("http://") || crawlDbUri.startsWith("https://")) {
      new RestHighLevelClient(
        RestClient.builder(
          new HttpHost(hostname, port, "http"),  // TODO: add https?
        )
      );
    } else if (crawlDbUri.startsWith("file://")) {
      ???  // TODO: embedded ES?
    } else if (crawlDbUri.contains("::")){
      ???  // TODO: cloudmode with zookeepers ES?
    } else {
      throw new RuntimeException(s"$crawlDbUri not supported")
    }


  }

  def getClient(): RestHighLevelClient = {
    crawlDb
  }

  def addResourceDocs(docs: java.util.Iterator[_]): Unit = ???

  def addResources(beans: java.util.Iterator[_]): Unit = ???

  def addResource(doc: Any): Unit = ???

  def commitCrawlDb(): Unit = ???

  def close(): Unit = ???
}
