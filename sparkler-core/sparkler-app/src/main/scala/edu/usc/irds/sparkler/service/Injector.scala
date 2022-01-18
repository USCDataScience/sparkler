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

package edu.usc.irds.sparkler.service
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.pipeline.UrlInjectorFunction
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import org.apache.commons.validator.routines.UrlValidator
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.StringArrayOptionHandler

import java.io.File
import java.nio.file.NotDirectoryException
import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, Stack}
import scala.io.Source

/**
  *
  * @since 5/28/16
  */
class Injector extends CliTool {

  import Injector.LOG

  // Load Sparkler Configuration
  val conf: SparklerConfiguration = Constants.defaults.newDefaultConfig()

  // Initialize URL Validator
  val urlValidator: UrlValidator = new UrlValidator()

  @Option(name = "-sf", aliases = Array("--seed-file"), forbids = Array("-su"),
    usage = "path to seed file")
  var seedFile: File = _

  @Option(name = "-su", aliases = Array("--seed-url"), usage = "Seed Url(s)",
    forbids = Array("-sf"), handler = classOf[StringArrayOptionHandler])
  var seedUrls: Array[String] = _

  @Option(name = "-id", aliases = Array("--job-id"),
    usage = "Id of an existing Job to which the urls are to be injected. No argument will create a new job")
  var jobId: String = ""

  @Option(name = "-idf", aliases = Array("--job-id-file"),
    usage = "A file containing the job id to be used in the crawl")
  var jobIdFile: String = ""

  @Option(name = "-cdb", aliases = Array("--crawldb"),
    usage = "Crawdb URI.")
  var sparkStorage: String = conf.getDatabaseURI

  @Option(name = "-co", aliases = Array("--config-override"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverride: Array[Any] = Array()

  @Option(name = "-co64", aliases = Array("--config-override-encoded"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverrideEncoded: String = ""

  @Option(name = "-cof", aliases = Array("--config-override-file"),
    handler = classOf[StringArrayOptionHandler],
    usage = "Configuration override. JSON Blob, key values in this take priority over config values in the config file.")
  var configOverrideFile: String = ""


  override def run(): Unit = {
    if (configOverride != ""){
      conf.overloadConfig(configOverride.mkString(" "));
    }
    if(configOverrideEncoded != ""){
      import java.nio.charset.StandardCharsets
      import java.util.Base64
      val decoded = Base64.getDecoder().decode(configOverrideEncoded)
      val str = new String(decoded, StandardCharsets.UTF_8)
      conf.overloadConfig(str)
    }
    if(configOverrideFile!= ""){
      val fileContents = Source.fromFile(configOverrideFile).getLines.mkString
      conf.overloadConfig(fileContents)
    }
    if (sparkStorage != null && !sparkStorage.isEmpty) {
      val uri = conf.asInstanceOf[java.util.HashMap[String, String]]
      uri.put("crawldb.uri", sparkStorage)
    }

    if (jobId.isEmpty && jobIdFile.isEmpty) {
      jobId = JobUtil.newJobId()
    } else if(!jobIdFile.isEmpty){
      jobId = Source.fromFile(jobIdFile).getLines.mkString
    }
    val job = new SparklerJob(jobId, conf)
    val storageFactory = job.getStorageFactory

    val urls: util.Collection[String] =
      if (seedFile != null) {
        if (seedFile.isFile) {
          Source.fromFile(seedFile).getLines().toList
        } else {
          stackListFiles(seedFile).par.flatMap((file) => Source.fromFile(file).getLines()).toList
        }
      } else {
        seedUrls.toList
      }

    val replacedUrls = UrlInjectorFunction(job, urls)
    // TODO: Add URL normalizer and filters before injecting the seeds
    var seeds = List[Resource]()
    replacedUrls.forEach(n => {
        if(urlValidator.isValid(n.getUrl)){
          val res = new Resource(n.getUrl.trim, 0, job, ResourceStatus.UNFETCHED, Injector.SEED_PARENT, Injector.SEED_SCORE, n.getMetadata, n.getHttpMethod)
          seeds = res :: seeds
        }
    })
    LOG.info("Injecting {} seeds", seeds.size())

    val storageProxy = storageFactory.getProxy
    storageProxy.addResources(seeds.iterator())
    storageProxy.commitCrawlDb()
    storageProxy.close()
  }

  override def parseArgs(args: Array[String]): Unit = {
    super.parseArgs(args)
    if (seedFile == null && seedUrls == null) {
      cliParser.printUsage(Console.out)
      throw new RuntimeException("either -sf or -su should be specified")
    }
  }

  /**
    * @note
    * This function is used to list all the text files in the directory provided
    * as a parameter to this function. This function uses a stack to read all
    * the subdirectories in the directory provided and uses a ArrayBuffer to collect
    * all the text files in the directory. Stack is chosen to avoid StackOverFlow
    * Exceptions.
    * @param directory directory that needs to be read
    * @return Array[File] Containing all extracted text(.txt) files
    * @throws NotDirectoryException is thrown if provided file is not a directory
    */
  def stackListFiles(directory: File): Array[File] = {
    if (!directory.isDirectory) throw new NotDirectoryException(directory.getName + " is not a directory")
    val stack = Stack[File](directory)
    val arrayBuffer = ArrayBuffer[File]()
    while (stack.nonEmpty) {
      val directory = stack.pop
      for (i <- directory.listFiles) {
        //TODO: Should this only read .txt files?
        if (i.isFile && i.getName.endsWith(".txt")) {
          arrayBuffer.append(i)
        }
        else if (i.isDirectory) stack.push(i)
      }
    }
    arrayBuffer.toArray
  }
}

object Injector extends Loggable {

  val SEED_PARENT = "seed"
  //val SEED_SCORE = 1.0
  val SMAP:Map[String,java.lang.Double] = Map("seed" -> 1.0.toDouble)

  val SEED_SCORE = new java.util.HashMap[String,java.lang.Double](SMAP)
  def main(args: Array[String]): Unit = {
    setLogLevel()
    val injector = new Injector()
    injector.run(args)
    println(s">>jobId = ${injector.jobId}")
  }
}
