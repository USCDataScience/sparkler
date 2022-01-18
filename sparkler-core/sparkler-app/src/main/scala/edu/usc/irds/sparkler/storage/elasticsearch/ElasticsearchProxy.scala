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

import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.Resource
import edu.usc.irds.sparkler.storage.StorageProxy
import org.apache.http.HttpHost
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.script.Script
import org.elasticsearch.xcontent.{XContentBuilder, XContentFactory}

import java.io.{Closeable, IOException}
import java.util.AbstractMap.SimpleEntry
import scala.collection.mutable.ArrayBuffer


/**
  *
  * @since 3/6/21
  */
class ElasticsearchProxy(var config: SparklerConfiguration) extends StorageProxy with Closeable with Loggable {

  // creates the client
  private var crawlDb = newClient(config.getDatabaseURI)

  private var indexRequests = ArrayBuffer[IndexRequest]()

  def newClient(crawlDbUri: String): RestHighLevelClient = {
    val scheme : String = crawlDbUri.substring(0, crawlDbUri.indexOf(':'))
    val hostname : String = crawlDbUri.substring(crawlDbUri.indexOf(':') + 3, crawlDbUri.lastIndexOf(':'))
    val port : Int = Integer.valueOf(crawlDbUri.substring(crawlDbUri.lastIndexOf(':') + 1))

    if (scheme.equals("http") || scheme.equals("https")) {
      new RestHighLevelClient(
        RestClient.builder(
          new HttpHost(hostname, port, scheme)
        )
      )
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
      while (docs.hasNext) {
        addResource(docs.next())
      }
    } catch {
      case e: ClassCastException => println("Must pass java.util.Iterator[Map[String, Object]] to ElasticsearchProxy.addResourceDocs")
    }
  }

  def addResources(resources: java.util.Iterator[Resource]): Unit = {
    var resource : Resource = null
    var dataMap : java.util.Map[String, Object] = null

    while (resources.hasNext) {
      try {
        resource = resources.next()
        dataMap = resource.getDataAsMap
      }
      catch {
        case e: IOException =>
          e.printStackTrace()
      }

      val indexRequest = new IndexRequest("crawldb")
      indexRequest.source(dataMap)
      indexRequest.id(resource.getId)

      indexRequests.append(indexRequest)
    }
  }

  def addResource(doc: Map[String, Object]): Unit = {
    try {
      val updateData : XContentBuilder = XContentFactory.jsonBuilder()
        .startObject()
      for ((key, value) <- doc) {
        value match {
          case value1: SimpleEntry[String, Object] =>
            // handle various commands besides setting
            // currently only increment ("inc") exists as of 4/25/2021
            val pair: SimpleEntry[String, Object] = value1
            if (pair.getKey == "inc") {
              // script to increment field
              val updateRequestForScripts: UpdateRequest = new UpdateRequest("crawldb",
                doc(Constants.storage.ID).asInstanceOf[String])

              val scriptCode: String = "ctx._source." + key + " += " + pair.getValue
              val newScript: Script = new Script(scriptCode)
              updateRequestForScripts.script(newScript)

              crawlDb.update(updateRequestForScripts, RequestOptions.DEFAULT)
              updateRequestForScripts.retryOnConflict(3)
            }
            else {
              println("ElasticsearchProxy: addResource() - unknown command in SimpleEntry[String, Object]")
            }
          case _ => if (key != Constants.storage.ID) updateData.field(key, value)
        }
      }
      updateData.endObject()

      val indexRequest : IndexRequest = new IndexRequest("crawldb", doc(Constants.storage.ID).asInstanceOf[String])
        .source(updateData)
      val updateRequest : UpdateRequest = new UpdateRequest("crawldb", doc(Constants.storage.ID).asInstanceOf[String])
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
