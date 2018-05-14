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

import org.kohsuke.args4j.{CmdLineException, CmdLineParser}

import scala.collection.JavaConversions._

/**
  *
  * @since 5/31/16
  */
trait CliTool extends Runnable {

  val cliParser = new CmdLineParser(this)

  def run(args: Array[String]): Unit = {
    parseArgs(args)
    run()
  }

  def parseArgs(ags: Array[String]): Unit = {
    try {
      cliParser.parseArgument(ags.toList)
    } catch {
      case e: CmdLineException =>
        Console.err.println(e.getMessage)
        cliParser.printUsage(System.err)
        System.exit(1)
    }
  }
}
