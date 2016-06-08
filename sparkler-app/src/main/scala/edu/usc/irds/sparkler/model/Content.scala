/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.usc.irds.sparkler.model

import java.util.Date

import edu.usc.irds.sparkler.model.ResourceStatus._
import org.apache.hadoop.conf.Configuration
import org.apache.nutch.metadata.Metadata

/**
  *
  * @since 5/29/16
  */
class Content(val url: String,
              val content: Array[Byte],
              val contentType: String,
              val contentLength: Long,
              val headers: Array[String],
              val fetchedAt: Date,
              val status: ResourceStatus,
              val metadata: Metadata) extends Serializable {

  // TODO: Move this to Util package
  def toNutchContent(conf: Configuration): org.apache.nutch.protocol.Content =
    new org.apache.nutch.protocol.Content(url, url, content, contentType, metadata, conf)
}
