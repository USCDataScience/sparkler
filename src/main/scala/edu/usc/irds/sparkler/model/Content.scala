package edu.usc.irds.sparkler.model

import java.util.Date

import ResourceStatus._
import org.apache.tika.metadata.Metadata

/**
  *
  * @since 5/29/16
  */
class Content(val url:String,
              val content:Array[Byte],
              val contentType:String,
              val contentLength:Long,
              val headers:Array[String],
              val fetchedAt:Date,
              val status:ResourceStatus,
              val metadata:Metadata) extends Serializable {}
