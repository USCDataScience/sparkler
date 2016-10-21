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
import java.net.{HttpURLConnection, URL}
import java.io.ByteArrayOutputStream

import edu.usc.irds.sparkler.{Fetcher, AbstractExtensionPoint}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{FetchedData}

import scala.language.postfixOps

/**
  * Default fetcher to downlod a url using HttpURLConnection
  */
class FetcherDefault extends AbstractExtensionPoint with Fetcher {

  val FETCH_TIMEOUT = 1000
  val DEFAULT_ERROR_CODE = 400
  import FetcherDefault.LOG

  override def fetch(url: String): FetchedData = {

    LOG.info("DEFAULT FETCHER {}", url)
    var responseCode = DEFAULT_ERROR_CODE
    try {
      val urlConn = new URL(url).openConnection()
      urlConn.setConnectTimeout(FETCH_TIMEOUT)
      responseCode = urlConn.asInstanceOf[HttpURLConnection].getResponseCode
      LOG.debug("STATUS CODE : " + responseCode + " " + url)

      val inStream = urlConn.getInputStream
      val outStream = new ByteArrayOutputStream()
      Iterator.continually(inStream.read)
        .takeWhile(-1 != )
        .foreach(outStream.write)
      inStream.close()

      val rawData = outStream.toByteArray
      outStream.close()

      new FetchedData(rawData,urlConn.getContentType,responseCode)
    } catch {
      case e: Exception =>
        LOG.warn("FETCH-ERROR {}", url)
        //e.printStackTrace()
        LOG.debug(e.getMessage, e)
        new FetchedData(Array[Byte](),"",responseCode)

    }
  }
}

object FetcherDefault extends Loggable {}
