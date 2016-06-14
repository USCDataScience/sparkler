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

import org.apache.hadoop.conf.Configuration
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Created by karanjeets on 6/12/16
 *
 * Utility to create Hadoop configuration that loads Sparkler resources
 */
object SparklerConfiguration {

  // Singleton. UUID Key and configuration file names
  val UUID_KEY = "sparkler.conf.uuid"
  val SPARKLER_DEFAULT = "sparkler-default.xml"
  val SPARKLER_SITE = "sparkler-site.xml"

  val LOG = LoggerFactory.getLogger(SparklerConfiguration.getClass())

  /**
   * To track the configuration instances created
   */
  def setUUID(conf: Configuration): Unit = {
    val uuid = UUID.randomUUID()
    conf.set(UUID_KEY, uuid.toString())
  }

  /**
   * Retrieves the UUID of this configuration object
   */
  def getUUID(conf: Configuration): String = {
    conf.get(UUID_KEY)
  }

  /**
   * Add Sparkler configuration
   */
  def addResources(conf: Configuration): Unit = {
    conf.addResource(SPARKLER_DEFAULT)
    conf.addResource(SPARKLER_SITE)
  }

  /**
   * Create configuration instance for Sparkler
   */
  def create(): Configuration = {
    val conf = new Configuration()
    setUUID(conf)
    addResources(conf)
    LOG.info("Sparkler Configuration Loaded Successfully")
    conf
  }
}
