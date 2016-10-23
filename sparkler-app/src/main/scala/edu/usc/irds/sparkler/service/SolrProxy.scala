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
import edu.usc.irds.sparkler.model.Resource
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.common.SolrInputDocument

/**
  *
  * @since 5/28/16
  */
class SolrProxy(var crawlDb: SolrClient) extends Closeable with Loggable {


  def addResourceDocs(docs: java.util.Iterator[SolrInputDocument]): Unit = {
    crawlDb.add(docs)
  }

  def addResources(beans: java.util.Iterator[_]): Unit = {
    try {
      crawlDb.addBeans(beans)
    } catch {
      case e: Exception =>
        LOG.debug("Caught {} while adding beans, trying to add one by one", e.getMessage)
        while (beans.hasNext) {
          val bean = beans.next()
          try { // to add one by one
            crawlDb.addBean(bean)
          } catch {
            case e2: Exception =>
              LOG.warn("(SKIPPED) {} while adding {}", e2.getMessage, bean)
              LOG.debug(e2.getMessage, e2)
          }
        }
    }
  }

  def commitCrawlDb(): Unit = {
    crawlDb.commit()
  }

  override def close(): Unit = {
    crawlDb.close()
  }
}
