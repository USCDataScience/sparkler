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

package edu.usc.irds.sparkler.base

import ch.qos.logback.classic.{Level, LoggerContext}
import edu.usc.irds.sparkler.base.Loggable.{selectedLogLevel, getLogger}

/**
  * Created by thammegr on 6/7/16.
  */
trait Loggable {

  val cl : String = getClass.getName
  println("++++++++++++++++++++++++++++++++++")
  println("Logable instance for: " + cl)
  println("++++++++++++++++++++++++++++++++++")

  val LOG = getLogger(getClass)

  def setLogLevel(): Unit = {

    val level = selectedLogLevel

    val newLevel: Level = level match {
      case "ALL" => Level.ALL
      case "DEBUG" => Level.DEBUG
      case "ERROR" => Level.ERROR
      case "TRACE" => Level.TRACE
      case "FATAL" => Level.WARN
      case "OFF" => Level.OFF
      case "INFO" => Level.INFO
      case _ => throw new IllegalArgumentException("Invalid log level provided")
    }

    LOG.setLevel(newLevel)
  }
}

object Loggable extends LoggerContext {
  var selectedLogLevel = "INFO"
}