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

import java.util.concurrent.atomic.AtomicLong

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model._

/**
  * Created by thammegr on 6/7/16.
  */
class FairFetcher(val job: SparklerJob, val resources: Iterator[Resource], val delay: Long,
                  val fetchFunc: ((SparklerJob, Iterator[Resource]) => Iterator[FetchedData]),
                  val parseFunc: ((CrawlData) => (ParsedData)),
                  val outLinkFilterFunc: ((SparklerJob, CrawlData) => (Set[String])))
  extends Iterator[CrawlData] {

  import FairFetcher.LOG

  val hitCounter = new AtomicLong()
  var lastHit: String = ""
  val fetchedData: Iterator[FetchedData] = fetchFunc(job, resources)

  override def hasNext: Boolean = fetchedData.hasNext

  override def next(): CrawlData = {

    val data = new CrawlData()
    val nextFetch = hitCounter.get() + delay
    val waitTime = nextFetch - System.currentTimeMillis()
    if (waitTime > 0) {
      LOG.debug("    Waiting for {} ms", waitTime)
      Thread.sleep(waitTime)
    }
    //STEP: Fetch
    val startTime = System.currentTimeMillis()
    data.fetchedData = fetchedData.next
    val endTime = System.currentTimeMillis()
    data.fetchedData.getResource.setFetchTimestamp(data.fetchedData.getFetchedAt)
    lastHit = data.fetchedData.getResource.getUrl
    if (data.fetchedData.getResponseTime < 0) {
      data.fetchedData.setResponseTime(endTime - startTime)
    }
    hitCounter.set(System.currentTimeMillis())

    //STEP: Parse
    data.parsedData = parseFunc(data)

    //STEP: URL Filter
    data.parsedData.outlinks = outLinkFilterFunc(job, data)
    data
  }
}

object FairFetcher extends Loggable {}
