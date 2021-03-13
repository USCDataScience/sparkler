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
import org.apache.solr.client.solrj.SolrClient
import org.elasticsearch.client.RestHighLevelClient

/**
  *
  * @since 3/2/2021
  */
abstract class StorageProxy() {

  // TODO: handle interaction between outside classes (RDD etc) and clients (SolrClient etc)
  //    with methods between StorageProxy and respective clients.
  //    outside classes should not be able to getClient

  def getClient(): Any
  def addResourceDocs(docs: java.util.Iterator[_]): Unit
  def addResources(beans: java.util.Iterator[_]): Unit
  def addResource(doc: Any): Unit

  def commitCrawlDb(): Unit
  def close(): Unit

}

