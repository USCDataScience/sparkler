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

import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.Date

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model.{Content, Resource}
import org.apache.nutch.metadata.Metadata

import scala.language.postfixOps

/**
  * Created by thammegr on 6/7/16.
  */
object FetchFunction extends ((Resource) => Content) with Serializable with Loggable {

  val FETCH_TIMEOUT = 1000

  override def apply(resource: Resource): Content = {
    LOG.info("FETCHING {}", resource.url)
    //FIXME: this is a prototype, make it real
    //TODO: handle errors
    val fetchedAt = new Date()
    val metadata = new Metadata()
    try {
      val urlConn = new URL(resource.url).openConnection()
      urlConn.setConnectTimeout(FETCH_TIMEOUT)

      val inStream = urlConn.getInputStream
      val outStream = new ByteArrayOutputStream()
      Iterator.continually(inStream.read)
        .takeWhile(-1 != )
        .foreach(outStream.write)
      inStream.close()

      val rawData = outStream.toByteArray
      outStream.close()
      val status: ResourceStatus = FETCHED
      val contentType = urlConn.getContentType
      new Content(resource.url, rawData, contentType, rawData.length, Array(),
        fetchedAt, status, metadata)
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        new Content(resource.url, Array(), "", -1, Array(), fetchedAt, ERROR, metadata)
    }
  }
}
