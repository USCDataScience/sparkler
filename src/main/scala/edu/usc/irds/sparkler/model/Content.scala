package edu.usc.irds.sparkler.model

import java.util.Date

import ResourceStatus._
import org.apache.hadoop.conf.Configuration
import org.apache.nutch.metadata.Metadata

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
              val metadata:Metadata) extends Serializable {

  def toNutchContent(conf:Configuration):org.apache.nutch.protocol.Content =
    new org.apache.nutch.protocol.Content(url, url, content, contentType, metadata, conf)
}
