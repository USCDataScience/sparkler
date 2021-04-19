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

package edu.usc.irds.sparkler.storage

import StatusUpdate.LOG
import edu.usc.irds.sparkler.Constants
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.spark.TaskContext

import scala.collection.JavaConversions._

/**
 * Created by karanjeets on 6/11/16
 */
class StatusUpdate(job: SparklerJob) extends ((TaskContext, Iterator[Map[String, Object]]) => Any) with Serializable {

  override def apply(context: TaskContext, docs: Iterator[Map[String, Object]]): Any = {
    LOG.debug("Updating document status into CrawlDb")
    val proxy = job.getStorageFactory().getProxy()
    proxy.addResourceDocs(docs)
    proxy.close()
  }
}

object StatusUpdate extends Loggable;
