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
import edu.usc.irds.sparkler.{AbstractExtensionPoint, ExtensionChain, URLFilter}

/**
  * If one of the URL filter rejects, then the url will be rejected
  */
class URLFilters extends AbstractExtensionPoint
    with ExtensionChain[URLFilter]
    with URLFilter
    with Loggable {

  var extensions:util.List[URLFilter] = _

  override def filter(url: String, parent: String): Boolean ={
    var status = true
    val iterator = extensions.iterator
    while (status && iterator.hasNext){
      try {
        status = iterator.next().filter(url, parent)
      } catch {
        case e: Exception =>
          LOG.warn(e.getMessage, e)
      }
    }
    status
  }

  override def setExtensions(extensions: util.List[URLFilter]): Unit = this.extensions = extensions

  override def getExtensions: util.List[URLFilter] = this.extensions

  override def getMinimumRequired: Int = 0

  override def getMaximumAllowed: Int = Byte.MaxValue
}


