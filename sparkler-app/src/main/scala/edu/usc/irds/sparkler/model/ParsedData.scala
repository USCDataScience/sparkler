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

package edu.usc.irds.sparkler.model

import org.apache.tika.metadata.Metadata

/**
  * Created by karanjeetsingh on 9/12/16.
  */
class ParsedData extends Serializable {
  var extractedText: String = _
  var outlinks: Set[String] = Set.empty[String]
  var metadata: Metadata = new Metadata()
  var headers: Map[String, AnyRef] = Map.empty
  var contentHash: String = _
}
