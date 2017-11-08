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

package edu.usc.irds.sparkler.pipeline

import com.google.common.hash.{HashFunction, Hashing}
import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.CrawlData
import edu.usc.irds.sparkler.solr.schema.FieldMapper
import org.apache.solr.common.SolrInputDocument

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  * Modified by karanjeets
  */
object ScoreUpdateSolrTransformer extends (CrawlData => SolrInputDocument ) with Serializable with Loggable {

  val fieldMapper: FieldMapper = FieldMapper.initialize()

  override def apply(data: CrawlData): SolrInputDocument = {
    val hashFunction: HashFunction = Hashing.sha256()
    val sUpdate = new SolrInputDocument()
    //FIXME: handle failure case
    sUpdate.setField(Constants.solr.ID, data.fetchedData.getResource.getId)

    val score: java.util.Map[java.lang.String, java.lang.Double] = data.fetchedData.getResource.getScore()

    score.foreach(pair => sUpdate.setField(pair._1, Map("set" -> pair._2).asJava))

    sUpdate
  }
}
