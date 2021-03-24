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
import edu.usc.irds.sparkler.storage.{StorageProxy, StorageProxyFactory}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.service.RejectingURLFilterChain
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.storage.elasticsearch.ElasticsearchProxy
import edu.usc.irds.sparkler.storage.solr.SolrProxy

import scala.collection.mutable

/**
  *
  * @since 5/31/16
  */
class SparklerJob(val id: String,
                  var config: SparklerConfiguration,
                  var currentTask: String)
  extends Serializable with JobContext with Loggable {

  /*
   * mappings from extension point to extension chain
   */
  //TODO: we should be able to overwrite these from config file
  val extChain: collection.mutable.HashMap[Class[_<:ExtensionPoint], Class[_<:ExtensionChain[_]]] =
  mutable.HashMap(
    (classOf[URLFilter], classOf[RejectingURLFilterChain])
  )

  def this(id: String, conf: SparklerConfiguration) {
    this(id, conf, JobUtil.newSegmentId())
  }

  def newStorageProxy(): StorageProxy = {
    new StorageProxyFactory(config).getProxy()
  }

  override def getConfiguration: SparklerConfiguration ={
    //FIXME: config has to be serializable
    if (config == null) {
      LOG.info("Empty Config Detected")
      config = Constants.defaults.newDefaultConfig()
    }
    this.config
  }

  override def getId: String = id

}

