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

package edu.usc.irds.sparkler.solr

import org.slf4j.LoggerFactory
import SolrUpsert.LOG
import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import org.apache.spark.TaskContext

import scala.collection.JavaConverters._
import org.apache.solr.client.solrj.SolrQuery


/**
 * Created by karanjeets on 6/11/16
 */
class SolrUpsert(job: SparklerJob) extends ((TaskContext, Iterator[Resource]) => Any) with Serializable {

  override def apply(context: TaskContext, docs: Iterator[Resource]): Any = {
    LOG.debug("Inserting new resources into CrawlDb")
    val solrClient = job.newCrawlDbSolrClient()

    //TODO: handle this in server side - tell solr to skip docs if they already exist
    var newResources: Set[Resource] = Set()
    for (doc <- docs) {
      val qry: SolrQuery = new SolrQuery(Constants.solr.STATUS + ":" + ResourceStatus.UNFETCHED)
      qry.addFilterQuery(s"${Constants.solr.DEDUPE_ID}:${doc.getDedupeId}")
      if (solrClient.crawlDb.query(qry).getResults.size() == 0) {
        newResources += doc
      }
    }
    LOG.info("Inserting new resources to Solr ")
    solrClient.addResources(newResources.iterator.asJava)
    LOG.debug("New resources inserted, Closing..")
    solrClient.close()
  }
}

object SolrUpsert {
  val LOG = LoggerFactory.getLogger(SolrUpsert.getClass())
}
