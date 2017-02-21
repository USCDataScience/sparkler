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

import java.io.ByteArrayInputStream
import java.net.HttpURLConnection

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{CrawlData, ParsedData}
import org.apache.commons.io.IOUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.{BodyContentHandler, LinkContentHandler, WriteOutContentHandler}

import scala.collection.JavaConverters._

/**
  * This is a transformation function for transforming raw data from crawler to parsed data
  */
object ParseFunction extends ((CrawlData) => (ParsedData)) with Serializable with Loggable {
  val redirectStatuses = Set(
    HttpURLConnection.HTTP_MOVED_PERM,
    HttpURLConnection.HTTP_MOVED_TEMP,
    HttpURLConnection.HTTP_SEE_OTHER)

  override def apply(data: CrawlData): (ParsedData) = {
    val parseData = new ParsedData()
    var stream = new ByteArrayInputStream(data.fetchedData.getContent)
    val linkHandler = new LinkContentHandler()
    val parser = new AutoDetectParser()
    var meta = new Metadata()
    val outHandler = new WriteOutContentHandler()
    val contentHandler = new BodyContentHandler(outHandler)
    LOG.info("PARSING  {}", data.fetchedData.getResource.getUrl)
    try {
      // Parse OutLinks
      meta.set("resourceName", data.fetchedData.getResource.getUrl)
      parser.parse(stream, linkHandler, meta)
      parseData.outlinks = linkHandler.getLinks.asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
      if (data.fetchedData.getHeaders.containsKey("Location")) {
        // redirect
        val redirectUrls = data.fetchedData.getHeaders.get("Location")
        parseData.outlinks ++= redirectUrls.asScala.filter(u => u != null && !u.isEmpty)
      }
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-OUTLINKS-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
    } finally { IOUtils.closeQuietly(stream) }
    try {
      meta = new Metadata
      meta.set("resourceName", data.fetchedData.getResource.getUrl)
      // Parse Text
      stream = new ByteArrayInputStream(data.fetchedData.getContent)
      parser.parse(stream, contentHandler, meta)
      parseData.extractedText = outHandler.toString
      parseData.metadata = meta
      parseData
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-CONTENT-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
        parseData
    } finally { IOUtils.closeQuietly(stream) }
  }
}
