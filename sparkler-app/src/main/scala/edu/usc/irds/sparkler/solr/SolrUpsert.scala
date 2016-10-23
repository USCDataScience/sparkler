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

import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.spark.TaskContext
import org.apache.solr.common.SolrInputDocument

import scala.collection.JavaConverters._
import edu.usc.irds.sparkler.model.Resource


/**
 * Created by karanjeets on 6/11/16
 */
class SolrUpsert(job: SparklerJob) extends ((TaskContext, Iterator[Resource]) => Any) with Serializable {

  override def apply(context: TaskContext, docs: Iterator[Resource]): Any = {
    LOG.debug("Inserting new resources into CrawlDb")
    val solrClient = job.newCrawlDbSolrClient()

    //TODO: handle this in server side - tell solr to skip docs if they already exist
    val newResources: Iterator[Resource] = for (doc <- docs if solrClient.crawlDb.getById(doc.getId) == null) yield doc
    LOG.info("Inserting new resources to Solr ")
    solrClient.addResources(newResources.asJava)
    LOG.debug("New resources inserted, Closing..")
    solrClient.close()
  }
}

object SolrUpsert {
  val LOG = LoggerFactory.getLogger(SolrUpsert.getClass())
}
