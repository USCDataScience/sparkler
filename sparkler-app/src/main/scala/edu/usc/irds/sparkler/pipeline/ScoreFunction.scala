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

import edu.usc.irds.sparkler.Scorer
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.service.PluginService
import edu.usc.irds.sparkler.util.FetcherDefault

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
  * Fetcher Function transforms stream of resources to fetched content.
  */
object ScoreFunction
  extends ((SparklerJob, CrawlData) => CrawlData)
    with Serializable with Loggable {

  override def apply(job: SparklerJob, data: CrawlData)
  : CrawlData = {
    val scorer:scala.Option[Scorer] = PluginService.getExtension(classOf[Scorer], job)
    try {
      scorer match {
        case Some(scorer) =>
          val score = scorer.score(data.parsedData.extractedText)
          data.fetchedData.getResource.setGenerateScore(score)
          data
        case None =>
          LOG.info("Scoring is not performed")
          data
      }
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        data
    }
  }
}
