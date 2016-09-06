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

import java.util.Date

import edu.usc.irds.sparkler.model.{CrawlData, Resource}
import org.apache.solr.common.SolrInputDocument
import org.apache.tika.metadata.Metadata

import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  */
object StatusUpdateSolrTransformer extends (CrawlData => SolrInputDocument ) with Serializable {

  override def apply(data: CrawlData): SolrInputDocument = {
    val sUpdate = new SolrInputDocument()
    //FIXME: handle failure case
    //val x:java.util.Map[String, Object] = Map("ss" -> new Object).asJava
    sUpdate.setField(Resource.ID, data.res.id)
    sUpdate.setField(Resource.STATUS, Map("set" -> data.content.status.toString).asJava)
    sUpdate.setField(Resource.LAST_FETCHED_AT, Map("set" -> data.content.fetchedAt).asJava)
    sUpdate.setField(Resource.LAST_UPDATED_AT, Map("set" -> new Date()).asJava)
    sUpdate.setField(Resource.NUM_TRIES, Map("inc" -> 1).asJava)
    sUpdate.setField(Resource.NUM_FETCHES, Map("inc" -> 1).asJava)
    sUpdate.setField(Resource.TITLE, data.metadata.get("title"))
    sUpdate.setField(Resource.EXTRACTED_TEXT, data.extractedText)
    sUpdate
  }
}
