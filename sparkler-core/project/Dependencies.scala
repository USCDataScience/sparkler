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

import sbt._

object Dependencies {
  lazy val args4j = "args4j" % "args4j" % "2.0.29" // CLI
  lazy val banana = "com.lucidworks" % "banana" % "1.5.1" artifacts(Artifact("banana", "war", "war"))
  lazy val commonsValidator = "commons-validator" % "commons-validator" % "1.5.1"
  lazy val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.5.2"
  object Jackson {
    private val group = "com.fasterxml.jackson.core"
    private val version = "2.6.5"
    lazy val core = group % "jackson-core" % version
    lazy val databind = group % "jackson-databind" % version
  }
  lazy val jBrowserDriver = "com.machinepublishers" % "jbrowserdriver" % "0.16.4"
  object Jetty {
    private val group = "org.eclipse.jetty"
    private val version = "9.4.0.v20161208"
    lazy val server = group % "jetty-server" % version
    lazy val servlet = group % "jetty-servlet" % version
  }
  lazy val jsonSimple = "com.googlecode.json-simple" % "json-simple" % "1.1.1"
  lazy val jUnit = "junit" % "junit" % "4.12"
  lazy val jUnitInterface = "com.novocode" % "junit-interface" % "0.11"
  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "0.10.0.0"
  lazy val nutch = "org.apache.nutch" % "nutch" % "1.16"
  lazy val pf4j = "org.pf4j" % "pf4j" % "2.6.0"
  lazy val scalaMacrosParadise = "org.scalamacros" %% "paradise" % "2.1.1"
  object Slf4j {
    private val group = "org.slf4j"
    private val version = "1.7.30"
    lazy val api = group % "slf4j-api" % version
    lazy val log4j12 = group % "slf4j-log4j12" % version
  }
  lazy val snakeYaml = "org.yaml" % "snakeyaml" % "1.26"
  object Solr {
    private val group = "org.apache.solr"
    private val version = "8.5.0"
    lazy val core = group % "solr-core" % version
    lazy val solrj = group % "solr-solrj" % version
  }
  object Spark {
    private val group = "org.apache.spark"
    private val version = "3.0.1" // pre-built version available @ https://spark.apache.org/downloads.html
    lazy val core = group %% "spark-core" % version
    lazy val sql = group %% "spark-sql" % version
  }
  lazy val tikaParsers = "org.apache.tika" % "tika-parsers" % "1.24"
}