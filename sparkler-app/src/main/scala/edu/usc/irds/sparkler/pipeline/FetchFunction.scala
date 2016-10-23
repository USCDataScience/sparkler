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

import edu.usc.irds.sparkler.{Fetcher}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.ResourceStatus._
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.service.PluginService
import org.apache.nutch.metadata.Metadata

import scala.language.postfixOps

/**
  * Created by thammegr on 6/7/16.
  */
object FetchFunction extends ((SparklerJob, Resource) => Content) with Serializable with Loggable {

  val FETCH_TIMEOUT = 1000
  val fetcherDefault = new FetcherDefault()

  override def apply(job: SparklerJob, resource: Resource): Content = {
    LOG.info("FETCHING {}", resource.getUrl)
    //TODO: Fetcher Plugin Integrated. Improve on this. Handle Errors

    val fetchedAt = new Date()
    val metadata = new Metadata()
    try {
      val fetcher:scala.Option[Fetcher] = PluginService.getExtension(classOf[Fetcher], job)
      var fetchedData: FetchedData = new FetchedData()
      fetcher match {
        case Some(fetcher) => {
          fetchedData = fetcher.fetch(resource.getUrl)
          if (!(fetchedData.getResponseCode >=200 && fetchedData.getResponseCode < 300 ) ){ // If not fetched through plugin successfully
            fetchedData = fetcherDefault.fetch(resource.getUrl)
          }
        }
        case None => {
          LOG.info("Using Default Fetcher")
          fetchedData = fetcherDefault.fetch(resource.getUrl)
        }
      }

      val rawData: Array[Byte] = fetchedData.getContent
      val status: ResourceStatus = FETCHED
      val contentType = fetchedData.getContentType
      new Content(resource.getUrl, rawData, contentType, rawData.length, Array(),
        fetchedAt, status, metadata)
    } catch {
      case e: Exception =>
        LOG.warn("FETCH-ERROR {}", resource.getUrl)
        LOG.debug(e.getMessage, e)
        new Content(resource.getUrl, Array(), "", -1, Array(), fetchedAt, ERROR, metadata)
    }


  }
}
