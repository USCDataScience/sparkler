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

import edu.usc.irds.sparkler.SparklerConfiguration

import java.util.concurrent.atomic.AtomicLong
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model._
import edu.usc.irds.sparkler.util.HealthChecks
import org.apache.commons.io.FilenameUtils
import org.apache.solr.common.SolrInputDocument
import org.apache.tika.mime.{MimeType, MimeTypes}

import java.io.{File, FileOutputStream}
import java.net.URI
import java.nio.file.Paths

/**
  * Created by thammegr on 6/7/16.
  */
class FairFetcher(val job: SparklerJob, val resources: Iterator[Resource], val delay: Long,
                  val fetchFunc: ((SparklerJob, Iterator[Resource]) => Iterator[FetchedData]),
                  val parseFunc: ((CrawlData) => (ParsedData)),
                  val outLinkFilterFunc: ((SparklerJob, CrawlData)  => (Set[String])),
                  val solrUpdateFunction: (CrawlData => SolrInputDocument ))
  extends Iterator[CrawlData] {

  import FairFetcher.LOG

  val hitCounter = new AtomicLong()
  var lastHit: String = ""
  val fetchedData: Iterator[FetchedData] = fetchFunc(job, resources)

  override def hasNext: Boolean = {
    if(!HealthChecks.checkFailureRate(job)) {
      fetchedData.hasNext
    } else{
      false
    }
  }

  def persistDocument(data: CrawlData, jobContext: SparklerConfiguration): Unit = {
    LOG.info("Persisting Document")
    if (jobContext.containsKey("fetcher.persist.content.location")) {
      val uri = new URI(data.fetchedData.getResource.getUrl)
      val domain = uri.getHost
      val outputDirectory = Paths.get(jobContext.get("fetcher.persist.content.location").toString,
        data.fetchedData.getResource.getCrawlId, domain).toFile
      var outputFile :File = null
      if (jobContext.get("fetcher.persist.content.filename").toString == "hash") {
        val allTypes = MimeTypes.getDefaultMimeTypes
        val ctype = allTypes.forName(data.fetchedData.getContentType)
        val ext = ctype.getExtension
        if(ext == null || ext == "") {
          var ext = FilenameUtils.getExtension(data.fetchedData.getResource.getUrl)
          if (ext.contains("#") || ext.contains("?")) {
            val splits = ext.split("[#?]")
            ext = splits(0)
          }
          ext = "." + ext
        }

        if(data.fetchedData.getContenthash == null){
          outputFile = Paths.get(jobContext.get("fetcher.persist.content.location").toString,
            data.fetchedData.getResource.getCrawlId, domain, data.fetchedData.getResource.getDedupeId + ext).toFile
        } else{
          outputFile = Paths.get(jobContext.get("fetcher.persist.content.location").toString,
            data.fetchedData.getResource.getCrawlId, domain, data.fetchedData.getContenthash + ext).toFile
        }

      }
      else {
        outputFile = Paths.get(jobContext.get("fetcher.persist.content.location").toString,
          data.fetchedData.getResource.getCrawlId, domain, FilenameUtils.getName(data.fetchedData.getResource.getUrl)).toFile
      }
      LOG.info("Creating directory: " + outputFile.toPath.getParent.toString)
      outputFile.toPath.getParent.toFile.mkdirs()
      try {
        LOG.info("Writing to: " + outputFile.toString)
        val outputStream = new FileOutputStream(outputFile)
        try outputStream.write(data.fetchedData.getContent)
        finally if (outputStream != null) outputStream.close()
      }
    }
  }

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
      data.fetchedData.setSegment(job.currentTask)
      lastHit = data.fetchedData.getResource.getUrl
      if (data.fetchedData.getResponseTime < 0) {
        data.fetchedData.setResponseTime(endTime - startTime)
      }
      hitCounter.set(System.currentTimeMillis())

      //STEP: Parse
      data.parsedData = parseFunc(data)

      //STEP: URL Filter
      data.parsedData.outlinks = outLinkFilterFunc(job, data)
      persistDocument(data, job.getConfiguration)
      val doc = solrUpdateFunction(data)
      LOG.info("Adding doc to SOLR")
      job.newStorageProxy().addResource(doc)


    data

  }
}

object FairFetcher extends Loggable {}
