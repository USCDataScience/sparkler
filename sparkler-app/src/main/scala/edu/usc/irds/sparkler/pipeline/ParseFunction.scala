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
import java.text.{ParseException, SimpleDateFormat}
import java.util
import java.util.Date

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{CrawlData, ParsedData}
import org.apache.commons.io.IOUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.{BodyContentHandler, LinkContentHandler, WriteOutContentHandler}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * This is a transformation function for transforming raw data from crawler to parsed data
  */
object ParseFunction extends ((CrawlData) => (ParsedData)) with Serializable with Loggable {

  override def apply(data: CrawlData): (ParsedData) = {
    val parseData = new ParsedData()
    var stream = new ByteArrayInputStream(data.fetchedData.getContent)
    val linkHandler = new LinkContentHandler()
    val parser = new AutoDetectParser()
    var meta = new Metadata()
    val outHandler = new WriteOutContentHandler()
    val contentHandler = new BodyContentHandler(outHandler)
    LOG.info("PARSING  {}", data.fetchedData.getResource.getUrl)

    // parse outlinks
    try {
      // Parse OutLinks
      meta.set("resourceName", data.fetchedData.getResource.getUrl)
      parser.parse(stream, linkHandler, meta)
      parseData.outlinks = linkHandler.getLinks.asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-OUTLINKS-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
    } finally { IOUtils.closeQuietly(stream) }

    //parse main text content
    try {
      meta = new Metadata
      meta.set("resourceName", data.fetchedData.getResource.getUrl)
      // Parse Text
      stream = new ByteArrayInputStream(data.fetchedData.getContent)
      parser.parse(stream, contentHandler, meta)
      parseData.extractedText = outHandler.toString
      parseData.metadata = meta
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-CONTENT-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
        parseData
    } finally { IOUtils.closeQuietly(stream) }

    // parse headers
    val headers = data.fetchedData.getHeaders
    if (headers.containsKey("Location")) {   // redirect
      val redirectUrls = headers.get("Location")
      parseData.outlinks ++= redirectUrls.asScala.filter(u => u != null && !u.isEmpty)
    }
    parseData.headers = parseHeaders(headers)
    parseData
  }

  def parseHeaders(headers: util.Map[String, util.List[String]]): Map[String, AnyRef] = {
    val dateHeaders = Set("Date", "Last-Modified", "Expires")
    val intHeaders = Set("ContentLength")
    val dateFmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")

    val result = mutable.Map[String, AnyRef]()
    for (name <- headers.keySet()) {
      val values = headers.get(name)
      var parsed: AnyRef = values
      if (values.size() == 1){
        val value = values.get(0)
        parsed = value
        try {
          if (dateHeaders contains name) {
            parsed = parseDate(value)
          } else if (intHeaders contains name) {
            parsed = new java.lang.Long(value.toLong)
          }
        } catch {
          case e: Exception => LOG.debug(e.getMessage, e)
        } finally {
          result(name) = parsed
        }
      }
    }
    result.toMap
  }

  /**
    * Parse date string as per RFC7231 https://tools.ietf.org/html/rfc7231#section-7.1.1.1
    */
  val httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
  @throws[ParseException] //but be aware of errors
  def parseDate(dateStr:String): Date = httpDateFormat.parse(dateStr.trim)
}
