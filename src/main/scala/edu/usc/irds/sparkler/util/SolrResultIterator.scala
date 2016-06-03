/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.usc.irds.sparkler.util

import edu.usc.irds.sparkler.CrawlDbRDD
import edu.usc.irds.sparkler.model.Resource
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
  *
  * @since 5/29/16
  */
class SolrResultIterator[T] extends Iterator[T]{
  import SolrResultIterator.LOG

  var solr:SolrClient = _
  var query:SolrQuery = _
  var beanType:Class[T] = _

  var closeClient:Boolean = _
  var nextStart:Int = _
  var buffer:Int = _
  var limit:Long = _
  var currentPage:Iterator[T]= _
  var nextBean:Option[T] = _
  var numFound:Long = _
  var count:Long = 1

  def this (solr:SolrClient, query:SolrQuery, buffer:Int, beanType:Class[T],
            limit:Long = Long.MaxValue, closeClient:Boolean = false){
    this()
    this.solr = solr
    this.query = query
    this.beanType = beanType
    this.buffer = buffer
    this.limit = limit
    this.closeClient = closeClient
    this.nextStart = if (query.getStart != null) query.getStart else 0
    this.nextBean = getNextBean(true)
  }

  override def hasNext: Boolean = nextBean.isDefined

  override def next(): T = {
    val tmp = nextBean
    nextBean = getNextBean()
    count += 1
    tmp.get
  }

  private def getNextBean(forceFetch:Boolean = false): Option[T] ={
    if (forceFetch || (!currentPage.hasNext && nextStart < numFound)) {
      //there are more
      query.setStart(nextStart)
      try {
        LOG.debug("Query {}, Start = {}", query.getQuery, nextStart)
        val response = solr.query(query)
        numFound = response.getResults.getNumFound
        currentPage = response.getBeans(beanType).iterator()
        nextStart += response.getResults.size()
      } catch {
        case e:Exception =>
          throw new RuntimeException(e);
      }
    }

    if (count < limit && currentPage.hasNext) {
      Some(currentPage.next())
    } else {
      SolrResultIterator.LOG.debug("Reached the end of result set")
      if (closeClient){
        SolrResultIterator.LOG.debug("closing solr client.")
        solr.close()
      }
      None
    }
  }
}

object SolrResultIterator{
  val LOG = org.slf4j.LoggerFactory.getLogger(SolrResultIterator.getClass)

  def main(args: Array[String]) {
    val solrq = new SolrQuery("*:*")
    solrq.setRows(100)
    val crawlDbUrl = "http://localhost:8983/solr/crawldb"
    val topN = 5
/*
    val sc: SparkContext = new SparkContext(new SparkConf().setMaster("local").setAppName("test"))
    val rdd = new CrawlDbRDD(sc,
    "http://localhost:8983/solr/crawldb", maxGroups = 1, topN = 1)

    println(rdd.count())
    sc.stop()*/

    val batchSize = 100
    val query = new SolrQuery("status:NEW")
    query.setFilterQueries(s"${Resource.GROUP}:twitter.com")
    //query.set("sort", "order")
    query.setRows(batchSize)

    val itt = new SolrResultIterator[Resource](new HttpSolrClient(crawlDbUrl), query, batchSize,
       classOf[Resource], limit=topN, closeClient = true)
    println(itt.toList.size)


  }
}
