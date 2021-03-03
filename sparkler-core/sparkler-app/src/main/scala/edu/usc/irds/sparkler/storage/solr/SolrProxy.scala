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

import java.io.{Closeable, File}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.storage.StorageProxy
import edu.usc.irds.sparkler._

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.impl.{CloudSolrClient}
import org.apache.solr.core.CoreContainer
import org.apache.solr.common.SolrInputDocument

import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
  *
  * @since 5/28/16
  */
class SolrProxy(var config: SparklerConfiguration) extends StorageProxy with Closeable with Loggable {

  /**
    * Creates solr client based on the crawldburi
    * @return Solr Client
    */
  def newClient(crawlDbUri: String): SolrClient = {
    if (crawlDbUri.startsWith("http://") || crawlDbUri.startsWith("https://")) {
      new HttpSolrClient.Builder(crawlDbUri).build
    } else if (crawlDbUri.startsWith("file://")) {
      var solrHome = crawlDbUri.replace("file://", "")
      LOG.info("Embedded Solr, Solr Core={}", solrHome)
      val solrHomeFile = new File(solrHome)
      if (!solrHomeFile.exists()) {
        val msg = s"Solr Core $solrHome doesn't exists"
        LOG.warn(msg)
        throw new SparklerException(msg)
      }

      //parent directory is solr home
      solrHome = solrHomeFile.getParent
      //directory name is the core name
      val coreName = solrHomeFile.getName
      LOG.info(s"Loading Embedded Solr, Home=$solrHome, Core=$coreName")
      val coreContainer: CoreContainer = new CoreContainer(solrHome)
      coreContainer.load()
      new EmbeddedSolrServer(coreContainer, coreName)
    } else if (crawlDbUri.contains("::")){
      //Expected format = collection::zkhost1:port1,zkhost2:port2
      // usually cloud uri has multi ZK hosts separated by comma(,)
      val Array(collectionName, zkhosts) = crawlDbUri.split("::")
      LOG.info("Solr crawldb.uri:{}, Cloud Client: Collection:{} ZKHost={}", crawlDbUri, collectionName, zkhosts)
      val client = new CloudSolrClient.Builder().withZkHost(zkhosts).build()
      client.setDefaultCollection(collectionName)
      client
    } else {
      throw new RuntimeException(s"$crawlDbUri not supported")
    }
  }

  // creates the solr client
  private var crawlDb = newClient(config.getDatabaseURI())

  def getClient(): SolrClient = {
    return crawlDb
  }

  def addResourceDocs(docs: java.util.Iterator[SolrInputDocument]): Unit = {
    crawlDb.add(docs)
  }

  def addResources(beans: java.util.Iterator[_]): Unit = {
    try {
      crawlDb.addBeans(beans)
    } catch {
      case e: Exception =>
        LOG.warn("Caught {} while adding beans, trying to add one by one", e.getMessage)
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

  def addResource(doc: SolrInputDocument): Unit = {
    crawlDb.add(doc)
  }

  def commitCrawlDb(): Unit = {
    crawlDb.commit()
  }

  override def close(): Unit = {
    crawlDb.close()
  }
}
