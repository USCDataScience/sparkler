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
import edu.usc.irds.sparkler.storage.StorageRDD
import edu.usc.irds.sparkler.service.RejectingURLFilterChain
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext

import scala.collection.mutable

/**
  *
  * @since 5/31/16
  */
class SparklerJob(val id: String,
                  var config: SparklerConfiguration,
                  var currentTask: String)
  extends Serializable with JobContext with Loggable {

  var storageProxyFactory : StorageProxyFactory = new StorageProxyFactory(config)
  var rddDefaults : StorageRDD = storageProxyFactory.getRDDDefaults()
  var deepRDDDefaults : StorageRDD = storageProxyFactory.getDeepRDDDefaults()

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
    storageProxyFactory.getProxy()
  }

  def newRDD(sc: SparkContext,
             job: SparklerJob,
             sortBy: String = rddDefaults.DEFAULT_ORDER,
             generateQry: String = rddDefaults.DEFAULT_FILTER_QRY,
             maxGroups: Int = rddDefaults.DEFAULT_GROUPS,
             topN: Int = rddDefaults.DEFAULT_TOPN): RDD[Resource] = {
    storageProxyFactory.getRDD(sc, job, sortBy, generateQry, maxGroups, topN)
  }

  def newDeepRDD(sc: SparkContext,
                 job: SparklerJob,
                 sortBy: String = deepRDDDefaults.DEFAULT_ORDER,
                 generateQry: String = deepRDDDefaults.DEFAULT_FILTER_QRY,
                 maxGroups: Int = deepRDDDefaults.DEFAULT_GROUPS,
                 topN: Int = deepRDDDefaults.DEFAULT_TOPN,
                 deepCrawlHosts: Array[String] = new Array[String](0)): RDD[Resource] = {
    storageProxyFactory.getDeepRDD(sc, job, sortBy, generateQry, maxGroups, topN, deepCrawlHosts)
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

