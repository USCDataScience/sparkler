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

package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.CrawlDbRDD._
import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import edu.usc.irds.sparkler.pipeline.SerializableFunction2
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

import scala.collection.JavaConversions._

/**
  *
  * @since 5/28/16
  */
class CrawlDbRDD(sc:SparkContext,
                 job:SparklerJob,
                 sortBy:String="depth asc,score asc",
                 generateQry:String="status:NEW",
                 maxGroups:Int=1000,
                 topN:Int=1000)
  extends RDD[Resource](sc, Seq.empty) {

  //TODO: accept serializable solr factory

  assert(maxGroups > 0)

  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition:SolrGroupPartition = split.asInstanceOf[SolrGroupPartition]
    val batchSize = 100
    val query = new SolrQuery(generateQry)
    query.addFilterQuery(s"${Resource.GROUP}:${partition.group}")
    query.addFilterQuery(s"${Resource.JOBID}:${job.id}")
    query.set("sort", sortBy)
    query.setRows(batchSize)

    new SolrResultIterator[Resource](job.newCrawlDbSolrClient().crawlDb, query,
      batchSize, classOf[Resource],  closeClient = true, limit = topN)
  }

  override protected def getPartitions: Array[Partition] = {
    val qry = new SolrQuery(generateQry)
    qry.addFilterQuery(s"${Resource.JOBID}:${job.id}")
    qry.set("sort", sortBy)
    qry.set("group", true)
    qry.set("group.ngroups", true)
    qry.set("group.field", "group")
    qry.set("group.limit", 0)
    qry.setRows(maxGroups)
    val proxy = job.newCrawlDbSolrClient()
    val solr = proxy.crawlDb
    val groupRes = solr.query(qry).getGroupResponse.getValues.get(0)
    val grps = groupRes.getValues
    LOG.info(s"selecting ${grps.size()} out of ${groupRes.getNGroups}")
    val res = new Array[Partition](grps.size())
    for (i <- 0 until grps.size()){
      //TODO: improve partitioning : (1) club smaller domains, (2) support for multiple partitions for larger domains
      res(i) = new SolrGroupPartition(i, grps(i).getGroupValue)
    }
    proxy.close()
    res
  }
}

class SolrGroupPartition(val indx:Int, val group:String, val start:Int=0,
                         val end:Int=Int.MaxValue) extends Partition {
  override def index: Int = indx
}

class SolrSink(job: SparklerJob) extends
  SerializableFunction2[TaskContext, Iterator[SolrInputDocument], Any] {

  override def apply(context: TaskContext, docs: Iterator[SolrInputDocument]): Any = {
    println(Thread.currentThread().getName + " :: Indexing to solr")
    val solrClient = job.newCrawlDbSolrClient()
    solrClient.addResourceDocs(docs)
    solrClient.close()
  }
}


class UnSerializableSolrBeanSink[T](solrClient: SolrClient) extends //solrclient is not serialiable!
  SerializableFunction2[TaskContext, Iterator[T], Any] {

  override def apply(ctx: TaskContext, docs: Iterator[T]): Any = {
    println(Thread.currentThread().getName + " :: Indexing to solr")
    solrClient.addBeans(docs)
  }
}


object CrawlDbRDD {

  val LOG = org.slf4j.LoggerFactory.getLogger(classOf[CrawlDbRDD])
}
