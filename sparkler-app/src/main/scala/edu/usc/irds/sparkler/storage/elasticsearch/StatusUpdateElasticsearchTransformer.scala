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

package edu.usc.irds.sparkler.storage.elasticsearch

import java.util.Date
import java.text.SimpleDateFormat
import java.util.AbstractMap.SimpleEntry

import com.google.common.hash.{HashFunction, Hashing}
import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.CrawlData
import edu.usc.irds.sparkler.storage.FieldMapper
import edu.usc.irds.sparkler.util.URLUtil
import edu.usc.irds.sparkler.storage.StatusUpdateTransformer

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by Thamme Gowda on 6/7/16.
  * Modified by karanjeets
  */
object StatusUpdateElasticsearchTransformer extends (CrawlData => Map[String, Object] ) with Serializable with Loggable with StatusUpdateTransformer {
  LOG.debug("Update Elasticsearch Transformer Created")
  val fieldMapper: FieldMapper = FieldMapper.initialize()

  override def apply(data: CrawlData): Map[String, Object] = {
    val hashFunction: HashFunction = Hashing.sha256()
    var toUpdate : Map[String, Object] = Map(
      Constants.storage.ID -> data.fetchedData.getResource.getId,
      Constants.storage.STATUS -> data.fetchedData.getResource.getStatus,
      Constants.storage.FETCH_TIMESTAMP -> new SimpleDateFormat(Constants.defaultDateFormat).format(data.fetchedData.getFetchedAt),
      Constants.storage.LAST_UPDATED_AT -> new SimpleDateFormat(Constants.defaultDateFormat).format(new Date()),
      Constants.storage.RETRIES_SINCE_FETCH -> new SimpleEntry[String, Integer]("inc", 1),
      Constants.storage.EXTRACTED_TEXT -> data.parsedData.extractedText,
      Constants.storage.CONTENT_TYPE -> data.fetchedData.getContentType.split("; ")(0),
      Constants.storage.FETCH_STATUS_CODE -> data.fetchedData.getResponseCode.toString(),
      Constants.storage.SIGNATURE -> hashFunction.hashBytes(data.fetchedData.getContent).toString,
      Constants.storage.RELATIVE_PATH -> URLUtil.reverseUrl(data.fetchedData.getResource.getUrl),
      Constants.storage.OUTLINKS -> data.parsedData.outlinks.toArray,
      Constants.storage.SEGMENT -> data.fetchedData.getSegment
    )

    val splitMimeTypes = data.fetchedData.getContentType.toLowerCase().split(";")
    if (splitMimeTypes.contains(Constants.storage.WEBPAGE_MIMETYPE.toLowerCase())) {
      toUpdate = toUpdate + (Constants.storage.RAW_CONTENT -> new String(data.fetchedData.getContent))
    } else if (splitMimeTypes.contains(Constants.storage.JSON_MIMETYPE.toLowerCase())){
      toUpdate = toUpdate + (Constants.storage.RAW_CONTENT -> new String(data.fetchedData.getContent))
    }
    toUpdate = toUpdate + (Constants.storage.RESPONSE_TIME -> data.fetchedData.getResponseTime)
    for ((scoreKey, score) <- data.fetchedData.getResource.getScore) {
      toUpdate = toUpdate + (scoreKey -> score)
    }

    val md = data.parsedData.metadata
    val mdFields = md.names().map(name => (name, if (md.isMultiValued(name)) md.getValues(name) else md.get(name))).toMap

    var mapped = fieldMapper.mapFields(mdFields, true)
    for (k <- mapped.keySet()) {
      val key = if (Constants.storage.MD_SUFFIX == null || Constants.storage.MD_SUFFIX.isEmpty || k.endsWith(Constants.storage.MD_SUFFIX)) k else k + Constants.storage.MD_SUFFIX
      toUpdate = toUpdate + (key -> mapped(k))
    }

    mapped = fieldMapper.mapFields(data.parsedData.headers, true)
    for (k <- mapped.keySet()) {
      val key = if (Constants.storage.HDR_SUFFIX == null || Constants.storage.HDR_SUFFIX.isEmpty || k.endsWith(Constants.storage.HDR_SUFFIX)) k else k + Constants.storage.HDR_SUFFIX
      toUpdate = toUpdate + (key -> mapped(k))
    }

    toUpdate
  }
}
