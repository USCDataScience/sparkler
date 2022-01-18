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

package edu.usc.irds.sparkler.storage.solr

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.spark.TaskContext
import edu.usc.irds.sparkler.storage.Upserter


import scala.collection.JavaConversions._


/**
 * Created by karanjeets on 6/11/16
 */
class SolrUpsert(job: SparklerJob) extends ((TaskContext, Iterator[Resource]) => Any) with Serializable with Upserter {

  import edu.usc.irds.sparkler.storage.solr.SolrUpsert.LOG

  override def apply(context: TaskContext, docs: Iterator[Resource]): Any = {
    LOG.debug("SolrUpsert - Inserting new resources into CrawlDb")
    val proxy = job.getStorageFactory.getProxy
    var client : SolrClient = null
    try {
      client = proxy.getClient().asInstanceOf[SolrClient]
    } catch {
      case e: ClassCastException => println("client is not SolrClient.")
    }

    //TODO: handle this in server side - tell solr to skip docs if they already exist

    //This filter function returns true if there is no other resource  with the same dedupe_id
    val newLinksFilter: (Resource => Boolean) = doc => {
      val qry = new SolrQuery(s"${Constants.storage.DEDUPE_ID}:${doc.getDedupeId}")
      qry.setRows(0) //we are interested in counts only and not the contents
      client.query(qry).getResults.getNumFound == 0
      // if zero hits, then there are no duplicates
    }
    val newResources = docs.withFilter(newLinksFilter)

    LOG.info("Inserting new resources to Solr ")
    proxy.addResources(newResources)
    LOG.debug("New resources inserted, Closing..")
    proxy.close()
  }
}

object SolrUpsert extends Loggable
