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

import edu.usc.irds.sparkler.{Constants, JobContext, SparklerConfiguration}
import edu.usc.irds.sparkler.service.SolrProxy
import edu.usc.irds.sparkler.util.JobUtil
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
  *
  * @since 5/31/16
  */
class SparklerJob(val id: String, @transient var config: SparklerConfiguration, var currentTask: String)
      extends Serializable with JobContext {

  var crawlDbUri: String = config.get(Constants.key.CRAWLDB).toString()

  def this(id: String, conf: SparklerConfiguration) {
    this(id, conf, JobUtil.newSegmentId())
  }

  def solrClient(): SolrProxy = {
    if (!crawlDbUri.startsWith("http://") && !crawlDbUri.startsWith("https://")) {
      throw new RuntimeException(s"$crawlDbUri not supported")
    }
    new SolrProxy(new HttpSolrClient(crawlDbUri))
  }

  override def getConfiguration: SparklerConfiguration ={
    //FIXME: config has to be serializable
    //FIXME: remove transient annotation from config and remove this reinitialization
    if (config == null) {
      config = Constants.defaults.newDefaultConfig()
    }
    this.config
  }

  override def getId: String = {
    id
  }

}

