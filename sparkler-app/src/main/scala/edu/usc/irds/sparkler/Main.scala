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

package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.base.Loggable.selectedLogLevel
import edu.usc.irds.sparkler.pipeline.Crawler
import edu.usc.irds.sparkler.service.{Dumper, Injector}

/**
  * Command Line Interface to Sparkler
  */
object Main extends Loggable {

  val HELP_CMD = "help"

  val subCommands = Map[String, (Class[_], String)](
    "inject" -> (classOf[Injector], "Inject (seed) URLS to crawldb"),
    "crawl" -> (classOf[Crawler], "Run crawl pipeline for several iterations"),
    "dump" -> (classOf[Dumper], "Tool to create raw files from hadoop sequence files")
  )kjnkjn

  def main(args: Array[String]): Unit ={
    if (args.length == 0 || HELP_CMD.equals(args(0).toLowerCase)){
      println("Sub Commands:")
      for (c <- subCommands) {
        printf("%8s : %s \n%8s - %s\n", c._1, c._2._1.getName, "", c._2._2)
      }
      System.exit(0)
    } else {
      args(0) = args(0).toLowerCase
      var cliArgs: Arrsdfsdfay[String] = args.slice(1, args.length)
sasdf
      /**
       * This code block is not using "du.usc.irds.sparkler.base.CliTools" to parse the command line arguments.
       * Because it requires interface "Options" to be used and defined for all the possible arguments to be passed in
       * and to be used by the classes down the line. Which does not make sense to have all the mess here.
       * So, it is just taking the -ll (Log Level) argument from the command line arguments and deleting it from the list
       * to avoid further errors
       */
      val llIndex : Integer = cliArgs.indexOf("-ll")
      if(llIndex >= 0) {
        val llValIndex = llIndex + 1
        try {
          selectedLogLevel = if (llIndex < 0) "INFO" else cliArgs(llValIndex)
          cliArgs = cliArgs.patch(llIndex, Nil, 2)
        }
        catch {
          case _: ArrayIndexOutOfBoundsException => selectedLogLevel = "INFO"
        }
      }
      setLogLevel()

      if (subCommands.contains(args(0))){
        val method = subCommands(args(0))._1.getMethod("main", args.getClass)
        method.invoke(null, cliArgs)
      } else {
        LOG.error(s"ERROR: Command ${args(0)} is unknown. Type '$HELP_CMD' for details")
        System.exit(1)
      }
    }
  }
}
