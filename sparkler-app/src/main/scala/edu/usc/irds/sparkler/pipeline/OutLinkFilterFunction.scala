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

import edu.usc.irds.sparkler.URLFilter
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.service.PluginService
import org.apache.commons.validator.routines.UrlValidator

import scala.language.postfixOps

/**
  * OutLinkFilter Function filters stream of URLs.
  */
object OutLinkFilterFunction
  extends ((SparklerJob, CrawlData) => Set[String])
    with Serializable with Loggable {

  override def apply(job: SparklerJob, data: CrawlData)
  : Set[String] = {
    val outLinkFilter: scala.Option[URLFilter] = PluginService.getExtension(classOf[URLFilter], job)
    var filteredOutLinks: Set[String] = Set()
    val urlValidator: UrlValidator = new UrlValidator()
    for (url <- data.parsedData.outlinks) {
      val result = outLinkFilter match {
        case Some(urLFilter) => {
          try {
            urlValidator.isValid(url) && urLFilter.filter(url, data.fetchedData.getResource.getUrl)
          } catch {
            case e: Exception => {
              LOG.debug("Exception while filtering the Outlink: " + url, e)
              false
            }
          }
        }
        case None => true
      }
      if (result) {
        filteredOutLinks += url
      }
    }
    filteredOutLinks
  }
}
