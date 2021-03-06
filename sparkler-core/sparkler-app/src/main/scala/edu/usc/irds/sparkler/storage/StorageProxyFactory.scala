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

import edu.usc.irds.sparkler.storage.solr.SolrProxy
import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import edu.usc.irds.sparkler.storage.elasticsearch.ElasticsearchProxy

/**
  *
  * @since 3/2/2021
  */
class StorageProxyFactory(var config: SparklerConfiguration) {

  val dbToUse: String = config.get(Constants.key.CRAWLDB_BACKEND).asInstanceOf[String]

  def getProxy(): StorageProxy = {
    dbToUse match {
      case "elasticsearch" => new ElasticsearchProxy(config): ElasticsearchProxy
      case "solr" => new SolrProxy(config): SolrProxy
      case _ => new SolrProxy(config): SolrProxy  // TODO: check null?
    }
  }

}