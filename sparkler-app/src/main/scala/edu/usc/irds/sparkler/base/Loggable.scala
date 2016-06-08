package edu.usc.irds.sparkler.base

import org.slf4j.LoggerFactory

/**
  * Created by thammegr on 6/7/16.
  */
trait Loggable {

  lazy val LOG = LoggerFactory.getLogger(getClass)
}
