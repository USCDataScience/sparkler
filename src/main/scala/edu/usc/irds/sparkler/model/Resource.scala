package edu.usc.irds.sparkler.model

import java.net.URL
import java.util.Date

import org.apache.solr.client.solrj.beans.Field
import ResourceStatus.ResourceStatus

/**
  *
  * @since 5/28/16
  */
class Resource extends Serializable {

  //NOTE: keep the variable names in sync with solr schema and the constants below
  @Field var id: String = _
  @Field var group: String = _
  @Field var lastFetchedAt: Date = _
  @Field var numTries: Int = 0
  @Field var numFetches: Int = 0
  @Field var depth:Int = 0
  @Field var score:Double = 0.0
  @Field var status:String = ResourceStatus.NEW.toString
  @Field var lastUpdatedAt:Date = _

  def this(id: String, group: String) {
    this
    this.id = id
    this.group = group
  }

  def this(id: String, group: String, lastFetchedAt: Date) {
    this(id, group)
    this.lastFetchedAt = lastFetchedAt
  }

  def this(id: String, depth:Int, status:ResourceStatus) {
    this(id, new URL(id).getHost)
    this.depth = depth
    this.status = status.toString
  }

  def this(id: String, group: String, lastFetchedAt: Date, numTries: Int,
           numFetches: Int, status: ResourceStatus) {
    this(id, group, lastFetchedAt)
    this.numTries = numTries
    this.numFetches = numFetches
    this.status = status.toString
  }

  override def toString = s"Resource($id, $group, $lastFetchedAt, $numTries, $numFetches, $depth, $score, $status)"
}


object Resource{

  //fields
  val ID = "id"
  val GROUP = "group"
  val LAST_FETCHED_AT= "lastFetchedAt"
  val NUM_TRIES = "numTries"
  val NUM_FETCHES = "numFetches"
  val DEPTH = "depth"
  val SCORE = "score"
  val STATUS = "status"
  val LAST_UPDATED_AT = "lastUpdatedAt"
}

object ResourceStatus extends Enumeration with Serializable {
  type ResourceStatus = Value
  val NEW, FETCHED, FETCHING, ERROR, IGNORED = Value
}

