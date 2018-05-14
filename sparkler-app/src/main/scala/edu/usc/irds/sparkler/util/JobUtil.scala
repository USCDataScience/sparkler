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

import java.text.SimpleDateFormat
import java.util.Date

/**
  * This block contains a set of utilities related to Job
  * @since Sparkler 0.1
  */
object JobUtil {

  // date format used by nutch segment ids
  private val DATE_FMT = new SimpleDateFormat("yyyyMMddHHmmss")

  /**
    * Makes new Id for job
    * @return new Id for a job
    */
  def newJobId(): String = "sjob-" + System.currentTimeMillis()

  /**
    * Creates ID for bew segment
    * @param nutchCompatible whether the id should be in same format as nutch
    * @return nutch segment Id
    */
  def newSegmentId(nutchCompatible: Boolean = true): String =
    (if (nutchCompatible) "" else "sseg-") + DATE_FMT.format(new Date())

}
