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

import SolrStatusUpdate.LOG
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.spark.TaskContext
import org.apache.solr.common.SolrInputDocument

import scala.collection.JavaConversions._

/**
 * Created by karanjeets on 6/11/16
 */
class SolrStatusUpdate(job: SparklerJob) extends ((TaskContext, Iterator[SolrInputDocument]) => Any) with Serializable {

  override def apply(context: TaskContext, docs: Iterator[SolrInputDocument]): Any = {
    LOG.debug("Updating document status into CrawlDb")
    val solrClient = job.newCrawlDbSolrClient()
    solrClient.addResourceDocs(docs)
    solrClient.close()
  }
}

object SolrStatusUpdate extends Loggable;
