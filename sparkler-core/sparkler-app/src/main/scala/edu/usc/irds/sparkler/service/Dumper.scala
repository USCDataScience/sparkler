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

import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.file.Paths
import java.security.MessageDigest
import edu.usc.irds.sparkler.base.{CliTool, Loggable}
import edu.usc.irds.sparkler.service.Injector.setLogLevel
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.hadoop.io.SequenceFile.Reader
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.hadoop.util.ReflectionUtils
import org.apache.nutch.protocol.Content
import org.kohsuke.args4j.Option

/**
  * A tool to dump sequence file records to raw files
  */
class Dumper extends CliTool with Loggable {

  @Option(name = "-i", aliases = Array("--job-dir", "--in"), required = true,
    usage="Sparkler Output Directory Containing Sequence Files")
  var jobDir: String = _

  @Option(name = "-o", aliases = Array("--dump-root", "--out"), required = true,
    usage="Directory to store raw files")
  var dumpRoot: String = _

  @Option(name = "-sc", aliases = Array("--skip-content"), usage = "Writes the index file and skips content write step")
  var skipContent: Boolean = false

  val digest = MessageDigest.getInstance("MD5")
  val metaFile = "_INDEX"


  override def run(): Unit = {
    LOG.info(s"DUmping Sequence Files to raw files: $jobDir --> $dumpRoot" )
    LOG.warn(s"This may overwrite destination ($dumpRoot), and may fill the storage or exhaust its iNode numbers")
    LOG.info("Recommendation: Use Hadoop HDFS Java API to directly access files from sequence files instead.")

    val conf = new Configuration()
    val fileSys = FileSystem.get(conf)
    //NO Checksum to speed up! Enable later if somebody needs this feature

    fileSys.setVerifyChecksum(false)
    fileSys.setWriteChecksum(false)

    val jobPath = new Path(jobDir)
    val rootPath = new Path(dumpRoot)
    assert(fileSys.exists(jobPath), s"$jobDir does not exist")
    fileSys.mkdirs(rootPath)


    val files = fileSys.listFiles(jobPath, true)
    val seqFilePaths = collection.mutable.Buffer[Path]()
    while (files.hasNext){
      val file = files.next()
      if (file.getPath.getName.matches("part-\\d{5}")){
        seqFilePaths += file.getPath
      }
    }
    LOG.info(s"Found ${seqFilePaths.size} sequence file parts")
    dumpAll(seqFilePaths, rootPath, fileSys)
  }

  /***
    * Converts URL to local file
    * @param url url
    * @param baseDir parent directory
    * @return path
    */
  def urlToPath(url:URL, baseDir:String): String = {
    val hash = Hex.encodeHexString(digest.digest(url.toString.getBytes()))
    assert(hash.length >= 6, "longer hash expected")
    val (idx0, idx2, idx4, idx6) = (0, 2, 4, 6)
    Paths.get(baseDir, url.getHost, hash.substring(idx0, idx2),
      hash.substring(idx2, idx4), hash.substring(idx4, idx6), hash).toString
  }

  /***
    * Write data to a file
    * @param fileSys : file system HDFS API
    * @param data : data
    * @param path: target path
    */
  def writeFile(fileSys: FileSystem, data:Content, path: String): Unit ={
    val tgtPath = new Path(path)
    fileSys.mkdirs(tgtPath.getParent)
    val writer = fileSys.create(tgtPath)
    val reader = new ByteArrayInputStream(data.getContent)
    try {
      IOUtils.copy(reader, writer)
    } finally {
      IOUtils.closeQuietly(writer)
      IOUtils.closeQuietly(reader)
    }
  }

  def dumpAll(seqFiles:Seq[Path], rootPath:Path, fileSys: FileSystem): Unit = {
    val bufferSize = 4096
    val conf = fileSys.getConf
    val rootString = rootPath.toString

    // Index file
    val indexPath = new Path(new Path(rootPath, metaFile), System.currentTimeMillis() + ".tsv")
    LOG.info(s"Writing Index file to $indexPath")
    fileSys.mkdirs(indexPath.getParent)
    val indexStream = fileSys.create(indexPath, false)

    try {
      for (seqFile <- seqFiles) {
        LOG.info(s"Reading file $seqFile")
        var reader: SequenceFile.Reader = null
        try {
          reader = new SequenceFile.Reader(conf, Reader.file(seqFile), Reader.bufferSize(bufferSize), Reader.start(0))
          val url = ReflectionUtils.newInstance(reader.getKeyClass, conf).asInstanceOf[Text]
          val data = ReflectionUtils.newInstance(reader.getValueClass, conf).asInstanceOf[Content]
          while (reader.next(url, data)) {
            try {
              val rawPath = urlToPath(new URL(url.toString), rootString)
              if (!skipContent) {
                writeFile(fileSys, data, rawPath)
              }
              indexStream.writeBytes(s"$rawPath\t$url\t${data.getContentType}\t${data.getContent.length}\n")
            } catch {
              case e: Exception =>
                LOG.warn(s"${e.getMessage} while writing $url ", e)
            }
          }
        } finally {
          IOUtils.closeQuietly(reader)
        }
      }
    } finally {
      IOUtils.closeQuietly(indexStream)
    }
  }
}

object Dumper {
  def main(args: Array[String]): Unit = {
    setLogLevel()
    new Dumper().run(args)
  }
}
