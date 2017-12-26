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

import java.util

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.{ExtensionChain, JobContext, SparklerException, URLFilter}

import scala.collection.JavaConversions._
/**
  * This class chains a list of URLFilters.
  *  How: If one of the URL filter rejects an url, then that url will be rejected.
  */
class RejectingURLFilterChain extends ExtensionChain[URLFilter]
    with URLFilter with Loggable {
  var context: JobContext = _
  var extensions:util.List[URLFilter] = _

  override def filter(url: String, parent: String): Boolean ={
    val res = !extensions.exists(ext => {
      try !ext.filter(url, parent)
      catch {
        case e:Exception =>
          LOG.warn(e.getMessage, e)
          false // exceptions are considered non rejections
      }
    })
    LOG.debug(s"$res : $url <-- $parent")
    res
  }

  override def getExtensions: util.List[URLFilter] = this.extensions
  /**
    * Setter for extensions
    *
    * @param extensions list of extensions
    */
  override def setExtensions(extensions: util.List[URLFilter]): Unit = this.extensions = extensions


  override def getMinimumRequired: Int = 0

  override def getMaximumAllowed: Int = Byte.MaxValue

  /**
    * Initialize the extension
    *
    * @param context job context
    * @throws SparklerException when an error occurs
    */
  override def init(context: JobContext, pluginId:String): Unit = {
    this.context = context
    LOG.info(s"Initializing ${this.getClass.getName} with ${size()} extensions: $getExtensions")
  }

}


