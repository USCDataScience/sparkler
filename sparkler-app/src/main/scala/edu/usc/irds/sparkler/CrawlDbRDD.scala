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
import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import edu.usc.irds.sparkler.solr.SolrGroupPartition
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}
import ClientUtils.escapeQueryChars
import org.apache.solr.client.solrj.SolrQuery.SortClause
import org.apache.solr.client.solrj.response.Group
import scala.collection.JavaConversions._

/**
  *
  * @since 5/28/16
  */
class CrawlDbRDD(sc: SparkContext,
                 job: SparklerJob,
                 sortBy: SortClause = CrawlDbRDD.DEFAULT_ORDER,
                 generateQry: String = CrawlDbRDD.DEFAULT_FILTER_QRY,
                 maxGroups: Int = CrawlDbRDD.DEFAULT_GROUPS,
                 topN: Int = CrawlDbRDD.DEFAULT_TOPN)
  extends RDD[Resource](sc, Seq.empty) {

  assert(topN > 0)
  assert(maxGroups > 0)

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    new SolrResultIterator[Resource](
      job.solrClient().crawlDb,
      new SolrQuery(generateQry)
        .addFilterQuery(s"""${Constants.solr.GROUP_ID}:"${escapeQueryChars(split.asInstanceOf[SolrGroupPartition].group)}"""")
        .addFilterQuery(s"${Constants.solr.JOBID}:${job.id}")
        .setRows(CrawlDbRDD.BATCH_SIZE)
        .setSort(sortBy),
      CrawlDbRDD.BATCH_SIZE, classOf[Resource], closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    job.solrClient().crawlDb.query(new SolrQuery(generateQry)
      .addFilterQuery(s"${Constants.solr.JOBID}:${job.id}")
      .setSort(sortBy)
      .setParam("group", true)
      .setParam("group.ngroups", true)
      .setParam("group.field", Constants.solr.GROUP_ID)
      .setParam("group.limit", "0")
      .setRows(maxGroups))
      .getGroupResponse.getValues.get(0)
      .getValues()
      .zipWithIndex
      .map({case (s: Group, i: Int) => new SolrGroupPartition(i, s.getGroupValue)})
      .toArray
  }
}

object CrawlDbRDD extends Loggable {
  val BATCH_SIZE = 10
  val DEFAULT_ORDER = SortClause.create("depth", SolrQuery.ORDER.asc) //+add: sort by lastFetch
  val DEFAULT_FILTER_QRY = "status:NEW"
  val DEFAULT_GROUPS = 1000
  val DEFAULT_TOPN = 1000
}
