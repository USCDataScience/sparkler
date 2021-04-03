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

package edu.usc.irds.sparkler.storage


import edu.usc.irds.sparkler.model.{Resource, SparklerJob}
import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import edu.usc.irds.sparkler.storage.solr.{SolrDeepRDD, SolrRDD, SolrProxy}
import edu.usc.irds.sparkler.storage.elasticsearch.{ElasticsearchDeepRDD, ElasticsearchRDD, ElasticsearchProxy}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  *
  * @since 3/2/2021
  */
class StorageProxyFactory(var config: SparklerConfiguration) {

  val dbToUse: String = config.get(Constants.key.CRAWLDB_BACKEND).asInstanceOf[String]

  def getProxy(): StorageProxy = {
      dbToUse match {
        case "elasticsearch" => new ElasticsearchProxy(config)
        case "solr" => new SolrProxy(config)
        case _ => new SolrProxy(config)
    }
  }

  def getDeepRDD(sc: SparkContext,
                 job: SparklerJob,
                 sortBy: String = SolrDeepRDD.DEFAULT_ORDER,
                 generateQry: String = SolrDeepRDD.DEFAULT_FILTER_QRY,
                 maxGroups: Int = SolrDeepRDD.DEFAULT_GROUPS,
                 topN: Int = SolrDeepRDD.DEFAULT_TOPN,
                 deepCrawlHosts: Array[String] = new Array[String](0)): RDD[Resource] = {
    dbToUse match {
      case "elasticsearch" => new ElasticsearchDeepRDD(sc, job, sortBy, generateQry, maxGroups, topN, deepCrawlHosts): ElasticsearchDeepRDD
      case "solr" => new SolrDeepRDD(sc, job, sortBy, generateQry, maxGroups, topN, deepCrawlHosts): SolrDeepRDD
      case _ => new SolrDeepRDD(sc, job, sortBy, generateQry, maxGroups, topN, deepCrawlHosts): SolrDeepRDD
    }
  }

  def getRDD(sc: SparkContext,
             job: SparklerJob,
             sortBy: String = SolrRDD.DEFAULT_ORDER,
             generateQry: String = SolrRDD.DEFAULT_FILTER_QRY,
             maxGroups: Int = SolrRDD.DEFAULT_GROUPS,
             topN: Int = SolrRDD.DEFAULT_TOPN): RDD[Resource] = {
    dbToUse match {
      case "elasticsearch" => new ElasticsearchRDD(sc, job, sortBy, generateQry, maxGroups, topN): ElasticsearchRDD
      case "solr" => new SolrRDD(sc, job, sortBy, generateQry, maxGroups, topN): SolrRDD
      case _ => new SolrRDD(sc, job, sortBy, generateQry, maxGroups, topN): SolrRDD
    }
  }

  def getRDDDefaults(): StorageRDD = {
    dbToUse match {
      case "elasticsearch" => ElasticsearchRDD
      case "solr" => SolrRDD
      case _ => SolrRDD
    }
  }

  def getDeepRDDDefaults(): StorageRDD = {
    dbToUse match {
    case "elasticsearch" => ElasticsearchDeepRDD
    case "solr" => SolrDeepRDD
    case _ => SolrDeepRDD
  }
  }
}