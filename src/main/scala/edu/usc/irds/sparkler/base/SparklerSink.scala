package edu.usc.irds.sparkler.base

import org.apache.nutch.protocol.Content

/**
  *
  * @since 5/31/16
  */
trait SparklerSink {

  def configure()
  def consume(jobId:String, iterationId:String, taskId:String, iterator: Iterator[Content])

}
