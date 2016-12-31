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

package edu.usc.irds.sparkler.service

import java.io.Closeable

import edu.usc.irds.sparkler.base.Loggable
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.common.SolrInputDocument
import edu.usc.irds.sparkler.model.Resource
import scala.collection.JavaConverters._

/**
  *
  * @since 5/28/16
  */
class SolrProxy(val crawlDb: SolrClient) extends Closeable with Loggable with Serializable {


  def addResourceDocs(docs: Iterator[SolrInputDocument]): SolrProxy = {
    crawlDb.add(docs.asJava)
    this
  }

  def addResources(beans: Iterator[Resource]): SolrProxy = {
    for (doc: Resource <- beans) {
      if (crawlDb.getById(doc.id) == null) {
        try {
          crawlDb.addBean(doc)
        } catch {
          case e2: Exception =>
            LOG.debug(e2.getMessage, e2)
        }
      }
    }
    this
  }

  def commit(): Unit = {
    crawlDb.commit()
  }

  override def close(): Unit = {
    crawlDb.close()
  }
}
