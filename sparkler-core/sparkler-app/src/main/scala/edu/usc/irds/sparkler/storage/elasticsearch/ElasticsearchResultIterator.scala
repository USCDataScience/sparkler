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

import org.elasticsearch.client.RestHighLevelClient

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest

import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.client.RequestOptions

import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit

import scala.collection.JavaConversions._

/**
  *
  * @since 4/10/21
  */
class ElasticsearchResultIterator[T] extends Iterator[T] {

  import ElasticsearchResultIterator.LOG

  var client: RestHighLevelClient = _
  var request: SearchRequest = _
  var scrollRequest: SearchScrollRequest = _
  var scrollId : String = ""
  var currentPage: Iterator[SearchHit] = _
  var nextBean: Option[SearchHit] = _
  var count: Long = 1
  var buffer: Int = _
  var limit: Long = _
  var beanType: Class[T] = _
  var closeClient: Boolean = _

  def this(client: RestHighLevelClient, request: SearchRequest, buffer: Int, beanType: Class[T],
           limit: Long = Long.MaxValue, closeClient: Boolean = false) {
    this()
    this.client = client
    this.request = request
    this.buffer = buffer
    this.beanType = beanType
    this.limit = limit
    this.closeClient = closeClient

    initializeScrollContext()
    this.nextBean = getNextBean(true)
  }

  private def initializeScrollContext(): Unit = {
    request.scroll(TimeValue.timeValueMinutes(1L))
    var searchResponse : SearchResponse = client.search(request, RequestOptions.DEFAULT)
    scrollId = searchResponse.getScrollId()
    currentPage = searchResponse.getHits().iterator()
  }

  private def getNextBean(dontFetch: Boolean = false): Option[SearchHit] = {
    if (!dontFetch && !currentPage.hasNext) {
      // fetch from elasticsearch using scroll api
      try {
        ElasticsearchResultIterator.LOG.debug("getNextBean(), scrollId = {}", scrollId)
        scrollRequest = new SearchScrollRequest(scrollId)
        scrollRequest.scroll(TimeValue.timeValueSeconds(30))
        var searchScrollResponse : SearchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT)
        scrollId = searchScrollResponse.getScrollId()
        currentPage = searchScrollResponse.getHits().iterator()
      } catch {
        case e: Exception =>
          throw new RuntimeException(e)
      }
    }

    if (count < limit && currentPage.hasNext) {
      Some(currentPage.next())
    } else {
      ElasticsearchResultIterator.LOG.debug("Reached the end of result set")
      if (closeClient) {
        ElasticsearchResultIterator.LOG.debug("closing elasticsearch client.")
        client.close()
      }
      None
    }
  }

  private def deserialize(searchHit: SearchHit): T = {
    searchHit.asInstanceOf[T] // placeholder to pass compile
    // NOTE: the template class must implement a constuctor for T(java.util.Map<String, Object>)
//    beanType.getConstructor(Map[String, Object].getClass).newInstance(searchHit.getSourceAsMap())
  }

  override def hasNext: Boolean = nextBean.isDefined

  override def next(): T = {
    val tmp = nextBean
    nextBean = getNextBean()
    count += 1
    deserialize(tmp.get)
  }
}

object ElasticsearchResultIterator {
  val LOG = org.slf4j.LoggerFactory.getLogger(ElasticsearchResultIterator.getClass)
}
