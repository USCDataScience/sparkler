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

import java.util.Properties

import edu.usc.irds.sparkler.service.SolrProxy
import edu.usc.irds.sparkler.util.JobUtil
import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
  *
  * @since 5/31/16
  */
class SparklerJob extends Serializable {

  import SparklerJob._

  var id: String = _
  var currentTask: String = _
  var settings: Properties = _
  var crawlDbUri: String = _

  def this(id: String, currentTask: String) {
    this()
    this.id = id
    this.currentTask = currentTask
    this.settings = SETTINGS
    this.crawlDbUri = settings.getProperty(CRAWLDB_KEY, CRAWLDB_DEFAULT_URI)
  }

  def this(id: String) {
    this(id, JobUtil.newSegmentId())
  }

  def newCrawlDbSolrClient(): SolrProxy = {
    if (crawlDbUri.startsWith("http://")) {
      return new SolrProxy(new HttpSolrClient(crawlDbUri))
    }

    throw new RuntimeException(s"$crawlDbUri not supported")
  }

}

object SparklerJob {

  val CRAWLDB_KEY = "sparkler.crawldb"
  val CRAWLDB_DEFAULT_URI = "http://localhost:8983/solr/crawldb"
  val DEFAULT_CONF = "sparkler-default.properties"
  val OVERRIDDEN_CONF = "sparkler-site.properties"
  val SETTINGS = new Properties()

  private var stream = getClass.getClassLoader.getResourceAsStream(DEFAULT_CONF)
  SETTINGS.load(stream)
  stream.close()

  stream = getClass.getClassLoader.getResourceAsStream(OVERRIDDEN_CONF)
  if (stream != null) {
    SETTINGS.load(stream)
    stream.close()
  }
}
