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
import scala.collection.mutable.ArrayBuffer
import java.io.IOException

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.storage.StorageProxy
import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.model.Resource

import org.apache.http.HttpHost
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType

import java.util.Collections.singletonMap
import java.util.AbstractMap.SimpleEntry


/**
  *
  * @since 3/6/21
  */
class ElasticsearchProxy(var config: SparklerConfiguration) extends StorageProxy with Closeable with Loggable {

  // creates the client
  private var crawlDb = newClient(config.getDatabaseURI())

  private var indexRequests = ArrayBuffer[IndexRequest]()

  def newClient(crawlDbUri: String): RestHighLevelClient = {
    val scheme : String = crawlDbUri.substring(0, crawlDbUri.indexOf(':'))
    val hostname : String = crawlDbUri.substring(crawlDbUri.indexOf(':')+3, crawlDbUri.lastIndexOf(':'))
    val port : Int = Integer.valueOf(crawlDbUri.substring(crawlDbUri.lastIndexOf(':') + 1))

    if (scheme.equals("http") || scheme.equals("https")) {
      new RestHighLevelClient(
        RestClient.builder(
          new HttpHost(hostname, port, scheme)
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

  def addResourceDocs(docs: java.util.Iterator[Map[String, Object]]): Unit = {
    try{
      while (docs.hasNext()) {
        addResource(docs.next())
      }
    } catch {
      case e: ClassCastException => println("Must pass java.util.Iterator[Map[String, Object]] to ElasticsearchProxy.addResourceDocs")
    }
  }

  def addResources(resources: java.util.Iterator[Resource]): Unit = {
    var resource : Resource = null
    var dataMap : java.util.Map[String, Object] = null

    while (resources.hasNext()) {
      try {
        resource = resources.next()
        dataMap = resource.getDataAsMap()
      }
      catch {
        case e: IOException =>
          e.printStackTrace()
      }

      var indexRequest = new IndexRequest("crawldb")
      indexRequest.source(dataMap)
      indexRequest.id(resource.getId())

      indexRequests.append(indexRequest)
    }
  }

  def addResource(doc: Map[String, Object]): Unit = {
    try {
      val updateData : XContentBuilder = XContentFactory.jsonBuilder()
        .startObject()
      for ((key, value) <- doc) {
        if (value.isInstanceOf[SimpleEntry[String, Object]]) {
          // handle various commands besides setting
          // currently only increment ("inc") exists as of 4/25/2021
          var pair : SimpleEntry[String, Object] = value.asInstanceOf[SimpleEntry[String, Object]]
          if (pair.getKey().asInstanceOf[String] == "inc") {
            // script to increment field
            var updateRequestForScripts : UpdateRequest = new UpdateRequest("crawldb",
              doc.get(Constants.storage.ID).get.asInstanceOf[String])

            var scriptCode : String = "ctx._source." + key + " += " + pair.getValue()
            var newScript : Script = new Script(scriptCode)
            updateRequestForScripts.script(newScript)

            crawlDb.update(updateRequestForScripts, RequestOptions.DEFAULT)
            updateRequestForScripts.retryOnConflict(3)
          }
          else {
            println("ElasticsearchProxy: addResource() - unknown command in SimpleEntry[String, Object]")
          }
        }
        else if (key != Constants.storage.ID) updateData.field(key, value)
      }
      updateData.endObject()

      var indexRequest : IndexRequest = new IndexRequest("crawldb", doc.get(Constants.storage.ID).get.asInstanceOf[String])
        .source(updateData)
      var updateRequest : UpdateRequest = new UpdateRequest("crawldb", doc.get(Constants.storage.ID).get.asInstanceOf[String])
        .doc(updateData)
        .upsert(indexRequest) // upsert either updates or insert if not found

      updateRequest.retryOnConflict(3)
      crawlDb.update(updateRequest, RequestOptions.DEFAULT)
    }
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  def commitCrawlDb(): Unit = {
    for (indexRequest <- indexRequests) {
      var response : IndexResponse = null
      try {
        response = crawlDb.index(indexRequest, RequestOptions.DEFAULT)
      }
      catch {
        case e: IOException =>
          e.printStackTrace()
      }
    }
    indexRequests = ArrayBuffer[IndexRequest]()  // clear indexRequests
  }

  def close(): Unit = {
    commitCrawlDb() // make sure buffer is flushed
    crawlDb.close()
  }

}
