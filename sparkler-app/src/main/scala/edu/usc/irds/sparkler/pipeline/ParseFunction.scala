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

import java.io.{InputStream, ByteArrayInputStream}

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{ParsedData, CrawlData}
import org.apache.commons.io.IOUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.{WriteOutContentHandler, BodyContentHandler, LinkContentHandler}

import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  */
object ParseFunction extends ((CrawlData) => (ParsedData)) with Serializable with Loggable {

  override def apply(data: CrawlData): (ParsedData) = {
    val parseData: ParsedData = new ParsedData()
    var stream: InputStream = new ByteArrayInputStream(data.content.content)
    val linkHandler = new LinkContentHandler()
    val parser = new AutoDetectParser()
    var meta = new Metadata()
    val outHandler = new WriteOutContentHandler()
    val contentHandler = new BodyContentHandler(outHandler)
    LOG.info("PARSING  {}", data.content.url)
    try {
      // Parse OutLinks
      meta.set("resourceName", data.content.url)
      parser.parse(stream, linkHandler, meta)
      parseData.outlinks = linkHandler.getLinks.asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
    } catch {
      case e:Throwable =>
        LOG.warn("PARSING-OUTLINKS-ERROR {}", data.content.url)
        LOG.warn(e.getMessage, e)
    } finally {
      IOUtils.closeQuietly(stream)
    }
    try {
      meta  = new Metadata
      meta.set("resourceName", data.content.url)
      // Parse Text
      stream = new ByteArrayInputStream(data.content.content)
      parser.parse(stream, contentHandler, meta)
      parseData.plainText = outHandler.toString
      parseData.metadata = meta
      parseData
    } catch {
      case e:Throwable =>
        LOG.warn("PARSING-CONTENT-ERROR {}", data.content.url)
        LOG.warn(e.getMessage, e)
        parseData
    } finally {
      IOUtils.closeQuietly(stream)
    }
  }
}
