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

package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.solr.SolrGroupPartition
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}
import ClientUtils.escapeQueryChars

import scala.collection.JavaConversions._

/**
  *
  * @since 5/28/16
  */
class CrawlDbRDD(sc: SparkContext,
                 job: SparklerJob,
                 sortBy: String = CrawlDbRDD.DEFAULT_ORDER,
                 generateQry: String = CrawlDbRDD.DEFAULT_FILTER_QRY,
                 maxGroups: Int = CrawlDbRDD.DEFAULT_GROUPS,
                 topN: Int = CrawlDbRDD.DEFAULT_TOPN,
                 groupBy: String = CrawlDbRDD.DEFAULT_GROUPBY)
  extends RDD[Resource](sc, Seq.empty) {


  assert(topN > 0)
  assert(maxGroups > 0)

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition: SolrGroupPartition = split.asInstanceOf[SolrGroupPartition]
    val batchSize = 100
    val query = new SolrQuery(generateQry)
    query.addFilterQuery(s"""${Constants.solr.GROUP}:"${escapeQueryChars(partition.group)}"""")
    query.addFilterQuery(s"${Constants.solr.CRAWL_ID}:${job.id}")
    query.set("sort", sortBy)
    query.setRows(batchSize)

    new SolrResultIterator[Resource](job.newCrawlDbSolrClient().crawlDb, query,
      batchSize, classOf[Resource], closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    val qry = new SolrQuery(generateQry)
    qry.addFilterQuery(s"${Constants.solr.CRAWL_ID}:${job.id}")
    qry.set("sort", sortBy)
    qry.set("group", true)
    qry.set("group.ngroups", true)
    qry.set("group.field", groupBy)
    qry.set("group.limit", 0)
    qry.setRows(maxGroups)
    val proxy = job.newCrawlDbSolrClient()
    val solr = proxy.crawlDb
    val groupRes = solr.query(qry).getGroupResponse.getValues.get(0)
    val grps = groupRes.getValues
    CrawlDbRDD.LOG.info(s"selecting ${grps.size()} out of ${groupRes.getNGroups}")
    val res = new Array[Partition](grps.size())
    for (i <- 0 until grps.size()) {
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SolrGroupPartition(i, grps(i).getGroupValue)
    }
    proxy.close()
    res
  }
}


object CrawlDbRDD extends Loggable {

  val DEFAULT_ORDER = Constants.solr.DISCOVER_DEPTH + " asc," + Constants.solr.SCORE + " asc"
  val DEFAULT_FILTER_QRY = Constants.solr.STATUS + ":" + ResourceStatus.UNFETCHED
  val DEFAULT_GROUPS = 1000
  val DEFAULT_TOPN = 1000
  val DEFAULT_GROUPBY = "group"
}
