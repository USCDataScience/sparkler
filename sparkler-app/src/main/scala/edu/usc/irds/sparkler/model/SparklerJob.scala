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

package edu.usc.irds.sparkler.model

import java.io.File

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.service.{RejectingURLFilterChain, SolrProxy}
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler._
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.impl.{CloudSolrClient, HttpSolrClient}
import org.apache.solr.core.CoreContainer

import scala.collection.mutable

/**
  *
  * @since 5/31/16
  */
class SparklerJob(val id: String,
                  @transient var config: SparklerConfiguration,
                  var currentTask: String)
  extends Serializable with JobContext with Loggable {

  var crawlDbUri: String = config.get(Constants.key.CRAWLDB).toString

  /*
   * mappings from extension point to extension chain
   */
  //TODO: we should be able to overwrite these from config file
  val extChain: collection.mutable.HashMap[Class[_<:ExtensionPoint], Class[_<:ExtensionChain[_]]] =
    mutable.HashMap(
      (classOf[URLFilter], classOf[RejectingURLFilterChain])
    )

  /**
    * Creates solr client based on the crawldburi
    * @return Solr Client
    */
  def newSolrClient(): SolrClient = {
    if (crawlDbUri.startsWith("http://") || crawlDbUri.startsWith("https://")) {
      new HttpSolrClient(crawlDbUri)
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

  def this(id: String, conf: SparklerConfiguration) {
    this(id, conf, JobUtil.newSegmentId())
  }

  def newCrawlDbSolrClient(): SolrProxy = {
    new SolrProxy(newSolrClient())
  }

  override def getConfiguration: SparklerConfiguration ={
    //FIXME: config has to be serializable
    //FIXME: remove transient annotation from config and remove this reinitialization
    if (config == null) {
      config = Constants.defaults.newDefaultConfig()
    }
    this.config
  }

  override def getId: String = id

}

