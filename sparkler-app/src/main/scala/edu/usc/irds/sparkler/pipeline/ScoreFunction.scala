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

/**
  * Fetcher Function transforms stream of resources to fetched content.
  */

class ScoreFunction(job:SparklerJob) extends (CrawlData => CrawlData) with Serializable with Loggable {

  var scorer:scala.Option[Scorer] = None

  def getScorer: Option[Scorer] = {
    //lazy initialization
    if (this.scorer.isEmpty){
      this.scorer = PluginService.getExtension(classOf[Scorer], job)
    }
    scorer
  }

  override def apply(data: CrawlData): CrawlData = {
    try {
      getScorer match {
        case Some(s) =>
          data.fetchedData.getResource.setScore(s.getScoreKey, s.score(data.parsedData.extractedText))
        case None =>
          LOG.debug("Scoring is not performed")
      }
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
    }
    data
  }
}
