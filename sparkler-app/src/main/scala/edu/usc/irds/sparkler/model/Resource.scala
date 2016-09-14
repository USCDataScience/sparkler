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

package edu.usc.irds.sparkler.model

import java.net.URL
import java.util.Date

import edu.usc.irds.sparkler.model.Resource._
import edu.usc.irds.sparkler.model.ResourceStatus.ResourceStatus
import org.apache.solr.client.solrj.beans.Field

/**
  *
  * @since 5/28/16
  */
class Resource extends Serializable {

  //NOTE: keep the variable names in sync with solr schema and the constants below
  @Field var id: String = _
  @Field var jobId: String = _
  @Field var url: String = _
  @Field var group: String = _
  @Field var lastFetchedAt: Date = _
  @Field var numTries: Int = 0
  @Field var numFetches: Int = 0
  @Field var depth: Int = 0
  @Field var score: Double = 0.0
  @Field var status: String = ResourceStatus.NEW.toString
  @Field var lastUpdatedAt: Date = _

  def this(url: String, group: String, job: SparklerJob) {
    this
    this.id = resourceId(url, job)
    this.url = url
    this.group = group
    this.jobId = job.id
  }

  def this(url: String, group: String, sparklerJob: SparklerJob, lastFetchedAt: Date) {
    this(url, group, sparklerJob)
    this.lastFetchedAt = lastFetchedAt
  }

  def this(url: String, depth: Int, sparklerJob: SparklerJob, status: ResourceStatus) {
    this(url, new URL(url).getHost, sparklerJob)
    this.depth = depth
    this.status = status.toString
  }

  def this(url: String, group: String, sparklerJob: SparklerJob, lastFetchedAt: Date, numTries: Int,
           numFetches: Int, status: ResourceStatus) {
    this(url, group, sparklerJob, lastFetchedAt)
    this.numTries = numTries
    this.numFetches = numFetches
    this.status = status.toString
  }

  override def toString:String = s"Resource($id, $group, $lastFetchedAt, $numTries, $numFetches, $depth, $score, $status)"
}


object Resource {

  //fields
  val ID = "id"
  val JOBID = "jobId"
  val URL = "url"
  val GROUP = "group"
  val LAST_FETCHED_AT = "lastFetchedAt"
  val NUM_TRIES = "numTries"
  val NUM_FETCHES = "numFetches"
  val DEPTH = "depth"
  val SCORE = "score"
  val STATUS = "status"
  val LAST_UPDATED_AT = "lastUpdatedAt"
  val PLAIN_TEXT = "plainText"
  val MD_SUFFIX = "_md"

  def resourceId(url: String, job: SparklerJob): String = s"${job.id}-$url"
}

object ResourceStatus extends Enumeration with Serializable {
  type ResourceStatus = Value
  val NEW, FETCHED, FETCHING, ERROR, IGNORED = Value
}

