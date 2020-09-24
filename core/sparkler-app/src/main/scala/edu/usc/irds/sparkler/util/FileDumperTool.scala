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

package edu.usc.irds.sparkler.util

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.Text
import org.apache.nutch.protocol.Content
import org.apache.spark.{SparkConf, SparkContext}
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.{BooleanOptionHandler, StringArrayOptionHandler}


/**
  * Created by shah on 5/16/17.
  */
class FileDumperTool extends CliTool with Serializable{

  import FileDumperTool._

  val APPNAME_KEY = "FileDumper"
  // Load Sparkler Configuration
  val sparklerConf: SparklerConfiguration = Constants.defaults.newDefaultConfig()

  @Option(name = "-m", aliases = Array("--master"),
    usage = "Spark Master URI. Ignore this if job is started by spark-submit")
  var sparkMaster: String = sparklerConf.get(Constants.key.SPARK_MASTER).asInstanceOf[String]

  @Option(name = "-i", aliases = Array("--input"),
    usage = "Path of input segment directory containing the part files", required = true)
  var inputDir: String = ""

  @Option(name = "-o", aliases = Array("--out"),
    usage = "Output path for dumped files", required = true)
  var outputPath: String = ""

  @Option(name = "-mf", aliases = Array("--mime-filter"), handler=classOf[StringArrayOptionHandler],
    usage = "A space separated list of mime-type to dump i.e files matching the given mime-types will be dumped, default no filter")
  var filter: Array[String] = Array()

  @Option(name = "--skip", handler=classOf[BooleanOptionHandler],
    usage = "Use this to skip dumping files matching the provided mime-types and dump the rest")
  var skip: Boolean = false

  @Option(name = "--mime-stats", handler=classOf[BooleanOptionHandler],
    usage = "Use this to skip dumping files matching the provided mime-types and dump the rest")
  var mimeStats: Boolean = false

  //TODO: Add functionality to dump a dir conaitning multiple segment directories.

  var job: SparklerJob = _
  var sc: SparkContext = _

  def init(): Unit = {
    val conf = new SparkConf().setAppName(APPNAME_KEY)
    if (!sparkMaster.isEmpty) {
      conf.setMaster(sparkMaster)
    }
    // For org.apache.hadoop.io.Text serialization
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    sc = new SparkContext(conf)

  }

  private def getCurrentTime(): String = {
    ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
  }

  override def run(): Unit = {
    LOG.info(s"Starting filedumper at ${getCurrentTime()}")
    init()
    LOG.info(s"Setting input directory to $inputDir")

    if (Files.exists(Paths.get(outputPath))) {
      LOG.error(s"$outputPath already exists, exiting file dumper")
      System.exit(1)
    }
    LOG.info(s"Setting output dir to $outputPath")

    if (mimeStats) {
      Dumper.getMimeStats(sc, inputDir)
      System.exit(0)
    }
    Dumper.dump(sc, inputDir, outputPath, filter, skip)
  }
}

object Dumper extends Loggable with Serializable {

  def dump(sc: SparkContext, inputDir: String, outputPath: String, filter: Array[String]
          , reverseFilter: Boolean): Unit = {
    var rdd = sc.sequenceFile(inputDir, classOf[Text], classOf[Content])
    if (filter.length > 0) {
      // If the content type of webpage matches the list return false
      if (reverseFilter) {
        rdd = rdd.filter({ case (url, content) =>
          var isFiltered = false
          filter.foreach(f => isFiltered = isFiltered | content.getContentType.equals(f))
          !isFiltered})
      }
      // If the content type of webpage matches the list return true
      else {
        rdd = rdd.filter({ case (url, content) =>
          var isFiltered = false
          filter.foreach(f => isFiltered = isFiltered | content.getContentType.equals(f))
          isFiltered})
      }
    }

    // dump the matching files to outputDir
    rdd.foreach({case (url, content) =>
      val filename = outputPath + "/" + URLUtil.reverseUrl(url.toString)
      val f = new File(filename)
      val parent = f.getParentFile
      parent.mkdirs
      FileUtils.writeByteArrayToFile(f, content.getContent)
      LOG.debug(s"Written $url to file $filename")
    })
  }

  def getMimeStats(sc: SparkContext, inputDir: String): Unit = {
    var rdd = sc.sequenceFile(inputDir, classOf[Text], classOf[Content])
    .map({case(url, content)=> (content.getContentType, 1)})
    .reduceByKey(_ + _)
    rdd.foreach({case (mimetype, count) => println(s"$mimetype : $count")})
  }
}

object FileDumperTool extends Loggable with Serializable {
  def main(args: Array[String]): Unit = {
    new FileDumperTool().run(args)
  }
}
