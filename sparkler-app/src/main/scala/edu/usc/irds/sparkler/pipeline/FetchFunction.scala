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

import edu.usc.irds.sparkler.util.FetcherDefault
import edu.usc.irds.sparkler.Fetcher
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.service.PluginService
import org.apache.nutch.metadata.Metadata

import scala.language.postfixOps
import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  */
object FetchFunction extends ((SparklerJob, Iterator[Resource]) => Iterator[FetchedData]) with Serializable with Loggable {

  val FETCH_TIMEOUT = 1000
  val fetcherDefault = new FetcherDefault()

  override def apply(job: SparklerJob, resources: Iterator[Resource]): Iterator[FetchedData] = {
    //LOG.info("FETCHING {}", resource.getUrl)
    //TODO: Fetcher Plugin Integrated. Improve on this. Handle Errors

    val fetchedAt = new Date()
    val metadata = new Metadata()
    var fetchedData: Iterator[FetchedData] = Iterator()
    val fetcher:scala.Option[Fetcher] = PluginService.getExtension(classOf[Fetcher], job)
    fetcher match {
      case Some(fetcher) => {
        fetchedData = fetcher.fetch(resources.asJava).asScala
        /*
        if (!(fetchedData.getResponseCode >=200 && fetchedData.getResponseCode < 300 ) ){ // If not fetched through plugin successfully
          fetchedData = fetcherDefault.fetch(resource.getUrl)
        } */
      }
      case None => {
        LOG.info("Using Default Fetcher")
        fetchedData = fetcherDefault.fetch(resources.asJava).asScala
      }
    }

    /*
    val rawData: Array[Byte] = fetchedData.getContent
    val status: ResourceStatus = FETCHED
    val contentType = fetchedData.getContentType
    new Content(resource.getUrl, rawData, contentType, rawData.length, Array(),
      fetchedAt, status, metadata)
    */
    fetchedData
  }
}
