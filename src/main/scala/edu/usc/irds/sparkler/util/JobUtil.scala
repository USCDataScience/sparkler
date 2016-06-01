package edu.usc.irds.sparkler.util

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

/**
  *
  * @since 5/31/16
  */
object JobUtil {

  val DATE_FMT = new SimpleDateFormat("yyyyMMddHHmmss")

  /**
    * Makes new Id for job
    * @return new Id for a job
    */
  def newJobId () = "sparkler-job-" + System.currentTimeMillis()

  def newSegmentId(nutchCompatible:Boolean = true) =
    (if (nutchCompatible) "" else "sparkler-seg-")+ DATE_FMT.format(new Date())



}
