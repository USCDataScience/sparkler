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
import java.text.{ ParseException, SimpleDateFormat }
import java.util
import java.util.Date

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{ SparklerJob, CrawlData, ParsedData }
import org.apache.commons.io.IOUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.{ BodyContentHandler, LinkContentHandler, WriteOutContentHandler }

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.immutable

import edu.usc.irds.sparkler.{ OutlinkParser, MetadataParser, HeaderParser, TextExtractParser }
import edu.usc.irds.sparkler.util.ParserDefault
import edu.usc.irds.sparkler.service.PluginService

/**
 * This is a transformation function for transforming raw data from crawler to parsed data
 */
object ParseFunction extends ((SparklerJob, CrawlData) => (ParsedData)) with Serializable with Loggable {

  override def apply(job: SparklerJob, data: CrawlData): (ParsedData) = {
    val parseData = new ParsedData()
    var stream = new ByteArrayInputStream(data.fetchedData.getContent)
    LOG.info("PARSING  {}", data.fetchedData.getResource.getUrl)
    var defaultParser = new ParserDefault()

    // parse outlinks
    parseData.outlinks = parseOutlink(data, job, defaultParser)

    // Parse Text
    //parse main text content
    var metadata: Metadata = new Metadata()
    parseData.extractedText = parseExtractText(data, job, defaultParser, metadata)

    // Parse Metadata
    parseData.metadata = parseMetadata(data, job, defaultParser, metadata)

    // parse headers

    var headerData: Map[String, util.List[String]] = data.fetchedData.getHeaders.asScala.toMap
    if (headerData.containsKey("Location")) { // redirect
      val redirectUrls = headerData.get("Location")
      parseData.outlinks ++= redirectUrls.asInstanceOf[Set[String]]
      //      parseData.outlinks ++= redirectUrls.asScala.filter(u => u != null && !u.isEmpty)
    }

    parseData.headers = parseHeader(data, job, defaultParser, headerData)
    parseData
  }

  def parseOutlink(data: CrawlData, job: SparklerJob, defaultParser: ParserDefault): (Set[String]) = {

    var stream = new ByteArrayInputStream(data.fetchedData.getContent)
    val outlinkParser: scala.Option[OutlinkParser] = PluginService.getExtension(classOf[OutlinkParser], job)
    var outlinks: Set[String] = Set.empty[String]
    try {
      // Parse OutLinks
      val url = data.fetchedData.getResource.getUrl

      outlinkParser match {
        case Some(p) => outlinks = p.parseOutlink(stream, url).asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
        case None => outlinks = defaultParser.parseOutlink(stream, url).asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
      }
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-OUTLINKS-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
    } finally { IOUtils.closeQuietly(stream) }
    outlinks
  }

  def parseExtractText(data: CrawlData, job: SparklerJob, defaultParser: ParserDefault, metadata: Metadata): (String) = {
    val textExtractParser: scala.Option[TextExtractParser] = PluginService.getExtension(classOf[TextExtractParser], job)
    var extractedText: String = ""

    var stream = new ByteArrayInputStream(data.fetchedData.getContent)
    try {
      metadata.set("resourceName", data.fetchedData.getResource.getUrl)

      textExtractParser match {
        case Some(tE) => extractedText = tE.parseText(stream, metadata)
        case None => extractedText = defaultParser.parseText(stream, metadata)
      }
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-CONTENT-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)

    } finally { IOUtils.closeQuietly(stream) }
    extractedText
  }

  def parseMetadata(data: CrawlData, job: SparklerJob, defaultParser: ParserDefault, metadata: Metadata): (Metadata) = {
    val metadataParser: scala.Option[MetadataParser] = PluginService.getExtension(classOf[MetadataParser], job)
    var stream = new ByteArrayInputStream(data.fetchedData.getContent)

    var meta: Metadata = new Metadata()
    try {
      val url = data.fetchedData.getResource.getUrl

      metadataParser match {
        case Some(m) => meta = m.parseMetadata(stream, metadata)
        case None => meta = defaultParser.parseMetadata(stream, metadata)
      }
    } catch {
      case e: Throwable =>
        LOG.warn("PARSING-CONTENT-ERROR {}", data.fetchedData.getResource.getUrl)
        LOG.warn(e.getMessage, e)
    } finally { IOUtils.closeQuietly(stream) }

    meta
  }

  def parseHeader(data: CrawlData, job: SparklerJob, default: ParserDefault, hData: Map[String, util.List[String]]): (Map[String, AnyRef]) = {

    val headerParser: scala.Option[HeaderParser] = PluginService.getExtension(classOf[HeaderParser], job)
    var headers: scala.collection.immutable.Map[String, AnyRef] = Map.empty
    try {
      headerParser match {
        case Some(h) => headers = h.parseHeader(hData).asScala.mapValues(_.asInstanceOf[AnyRef]).toMap
        case None => headers = default.parseHeader(hData).asScala.mapValues(_.asInstanceOf[AnyRef]).toMap
      }
    } catch {
      case e: Exception => LOG.debug(e.getMessage, e)
    } finally { headers }

    headers
  }
}
