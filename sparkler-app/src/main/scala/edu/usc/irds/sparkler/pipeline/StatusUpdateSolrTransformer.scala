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

import com.google.common.hash.{HashFunction, Hashing}
import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.model.{CrawlData, Resource}
import edu.usc.irds.sparkler.solr.schema.FieldMapper
import edu.usc.irds.sparkler.util.{StringUtil, URLUtil}
import org.apache.solr.common.SolrInputDocument

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by thammegr on 6/7/16.
  * Modified by karanjeets
  */
object StatusUpdateSolrTransformer extends (CrawlData => SolrInputDocument ) with Serializable {

  override def apply(data: CrawlData): SolrInputDocument = {
    val hashFunction: HashFunction = Hashing.sha256()
    val sUpdate = new SolrInputDocument()
    //FIXME: handle failure case
    //val x:java.util.Map[String, Object] = Map("ss" -> new Object).asJava
    sUpdate.setField(Constants.solr.ID, data.fetchedData.getResource.getId)
    sUpdate.setField(Constants.solr.STATUS, Map("set" -> data.fetchedData.getResource.getStatus).asJava)
    sUpdate.setField(Constants.solr.FETCH_TIMESTAMP, Map("set" -> data.fetchedData.getFetchedAt).asJava)
    sUpdate.setField(Constants.solr.LAST_UPDATED_AT, Map("set" -> new Date()).asJava)
    sUpdate.setField(Constants.solr.RETRIES_SINCE_FETCH, Map("inc" -> 1).asJava)
    //sUpdate.setField(Constants.solr.NUM_FETCHES, Map("inc" -> 1).asJava)
    sUpdate.setField(Constants.solr.EXTRACTED_TEXT, data.parsedData.extractedText)
    sUpdate.setField(Constants.solr.CONTENT_TYPE, data.fetchedData.getContentType.split("; ")(0))
    sUpdate.setField(Constants.solr.FETCH_STATUS_CODE, data.fetchedData.getResponseCode)
    sUpdate.setField(Constants.solr.SIGNATURE, hashFunction.hashBytes(data.fetchedData.getContent).toString)
    sUpdate.setField(Constants.solr.RELATIVE_PATH, URLUtil.reverseUrl(data.fetchedData.getResource.getUrl))
    sUpdate.setField(Constants.solr.OUTLINKS, data.parsedData.outlinks.toArray)

    var mdFields: Map[String, AnyRef] = Map()
    for (name: String <- data.parsedData.metadata.names()) {
      mdFields += (name -> (if (data.parsedData.metadata.isMultiValued(name)) data.parsedData.metadata.getValues(name) else data.parsedData.metadata.get(name)))
    }
    val fieldMapper: FieldMapper = FieldMapper.initialize()
    val mappedMdFields: mutable.Map[String, AnyRef] = fieldMapper.mapFields(mdFields.asJava, true).asScala
    mappedMdFields.foreach{case (k, v) => {
      var key: String = k
      if (!k.endsWith(Constants.solr.MD_SUFFIX)) {
        key = k + Constants.solr.MD_SUFFIX
      }
      sUpdate.setField(key, v)
    }}

    sUpdate
  }
}
