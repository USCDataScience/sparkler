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

package edu.usc.irds.sparkler.storage

import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.CrawlData

import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  * Modified by karanjeets
  */
object ScoreUpdateTransformer extends (CrawlData => Map[String, Object]) with Serializable with Loggable {

  override def apply(data: CrawlData): Map[String, Object] = {

    val toUpdate : Map[String, Object] = Map(
      Constants.storage.ID -> data.fetchedData.getResource.getId,
      Constants.storage.GENERATE_SCORE -> Map("set" -> data.fetchedData.getResource.getGenerateScore()).asJava
    )

    toUpdate
  }
}
