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

import scala.sys.process._
organization := Settings.projectOrganization
maintainer := Settings.projectMaintainer

// Scala/Java Build Options
// spark 3.0.1 now come pre-build with scala 2.12 @ https://spark.apache.org/downloads.html
javacOptions in (Compile, doc) in ThisBuild ++= Seq("-source", "8")
javacOptions in (Compile, compile) ++= Seq("-target", "8")
// Common dependencies
libraryDependencies in ThisBuild ++= Seq(
  Dependencies.pf4j % "provided",
)

developers := List(
  // In alphabetic order
  Developer("chrismattmann",
    "Chris Mattmann",
    "mattmann@apache.org ",
    url("https://github.com/chrismattmann")
  ),
  Developer("karanjeets",
    "Karanjeet Singh",
    "karanjeet@apache.org",
    url("https://github.com/karanjeets")
  ),
  Developer("buggtb",
    "Tom Barber",
    "tom@analytical-labs.com",
    url("https://github.com/buggtb")
  ),
  Developer("thammegowda",
    "Thamme Gowda",
    "thammegowda@apache.org",
    url("https://github.com/thammegowda")
  ),
  // Apologies if we missed you. Please add yourself here..
)

lazy val plugins = ProjectRef(file("./"), "plugins")

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.common,
    name := "sparkler",
    mainClass in Compile := Some("edu.usc.irds.sparkler.Main"),

  )
  .aggregate(api, app, plugins)

lazy val api = (project in file("sparkler-api"))
  .settings(
    Settings.common,
    name := "sparkler-api",
    libraryDependencies ++= Seq(
      Dependencies.jsonSimple exclude("junit", "junit"),
      Dependencies.nutch exclude("*", "*"),
      Dependencies.snakeYaml,
      Dependencies.Solr.solrj exclude("org.apache.spark", "spark-sql"),
      Dependencies.gson,

      // Test
      Dependencies.jUnit % Test,
      Dependencies.jUnitInterface % Test
    ),
    assemblyMergeStrategy in assembly := {
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("Log4j2Plugins.dat") => MergeStrategy.first
      case x if x.contains("module-info.class") => MergeStrategy.first
      case x if x.contains("public-suffix-list.txt") => MergeStrategy.first
      case x if x.contains("bus-extensions.txt") => MergeStrategy.first
      case x if x.contains("blueprint.handlers") => MergeStrategy.first
      case x if x.contains("git.properties") => MergeStrategy.first
      case x if x.contains("overview.html") => MergeStrategy.first
      case x if x.contains("config.fmpp") => MergeStrategy.first
      case x if x.contains("META-INF/versions/9/javax/xml/bind/") => MergeStrategy.first
      case x if x.contains("META-INF/native-image/io.netty") => MergeStrategy.first
      case PathList("org", "apache", "logging", "log4j", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "logging", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "log4j", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
      case PathList("org", "slf4j", "impl", xs@_*) => MergeStrategy.first
      case PathList("com", "ctc", "wstx", xs@_*) => MergeStrategy.first
      case PathList("org", "cliffc", "high_scale_lib", xs@_*) => MergeStrategy.first
      case PathList("javax.xml.bind", "jaxb-api", xs@_*) => MergeStrategy.first
      case PathList("org", "hamcrest", xs@_*) => MergeStrategy.first
      case PathList("javax", "xml", xs@_*) => MergeStrategy.first
      case PathList("javax", "activation", xs@_*) => MergeStrategy.first
      case PathList("io", "netty", xs@_*) => MergeStrategy.first
      case PathList("org", "aopalliance", "intercept", xs@_*) => MergeStrategy.first
      case PathList("org", "aopalliance", "aop", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "spark", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "hadoop", xs@_*) => MergeStrategy.first
      case PathList("net", "jpountz", xs@_*) => MergeStrategy.last
      case PathList("net", "jcip", xs@_*) => MergeStrategy.first
      case PathList("javax", "inject", xs@_*) => MergeStrategy.first
      case PathList("javax", "annotation", xs@_*) => MergeStrategy.first
      case PathList("com", "sun", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.first
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.first

      case x => (assemblyMergeStrategy in assembly).value.apply(x)
    },
    test in assembly := {},
    testOptions += Tests.Argument(TestFrameworks.JUnit,
      "--verbosity=1",
      "--run-listener=edu.usc.irds.sparkler.test.WebServerRunListener"),


  )
  .dependsOn(testsBase)


val sparkprovided = System.getProperty("sparkprovided", "")

lazy val app = (project in file("sparkler-app"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.common,
    name := "sparkler-app",
    mainClass in (Compile, packageBin) := Some("edu.usc.irds.sparkler.Main"),
    libraryDependencies ++= (
      if(sparkprovided == "true") {
        ("org.apache.spark" %% "spark-core" % "3.1.0" % "provided") :: Nil
        ("org.apache.spark" %% "spark-sql" % "3.1.0" % "provided") :: Nil
      } else {
        ("org.apache.spark" %% "spark-core" % "3.1.0") :: Nil
        ("org.apache.spark" %% "spark-sql" % "3.1.0") :: Nil
      }
    ),
    libraryDependencies ++= Seq(
      // TODO: Only keep necessary dependencies. Rest all should be included as plugin. Eg: extractors
      Dependencies.args4j,
      Dependencies.commonsValidator,
      Dependencies.Jackson.databind exclude("org.slf4j", "slf4j-api"),
      Dependencies.Jackson.core,
      Dependencies.kafkaClients exclude("org.slf4j", "slf4j-api"),
      Dependencies.pf4j,
      Dependencies.Solr.core,
      Dependencies.tikaParsers,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
      Dependencies.lz4,
      Dependencies.elasticsearch
    ),
    assemblyMergeStrategy in assembly := {
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("Log4j2Plugins.dat") => MergeStrategy.first
      case x if x.contains("module-info.class") => MergeStrategy.first
      case x if x.contains("public-suffix-list.txt") => MergeStrategy.first
      case x if x.contains("bus-extensions.txt") => MergeStrategy.first
      case x if x.contains("blueprint.handlers") => MergeStrategy.first
      case x if x.contains("git.properties") => MergeStrategy.first
      case x if x.contains("config.fmpp") => MergeStrategy.first
      case x if x.contains("META-INF/versions/9/javax/xml/bind/") => MergeStrategy.first
      case x if x.contains("META-INF/native-image/io.netty") => MergeStrategy.first
      case PathList("org", "apache", "logging", "log4j", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "logging", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "log4j", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
      case PathList("org", "slf4j", "impl", xs@_*) => MergeStrategy.first
      case PathList("com", "ctc", "wstx", xs@_*) => MergeStrategy.first
      case PathList("org", "cliffc", "high_scale_lib", xs@_*) => MergeStrategy.first
      case PathList("javax.xml.bind", "jaxb-api", xs@_*) => MergeStrategy.first
      case PathList("org", "hamcrest", xs@_*) => MergeStrategy.first
      case PathList("javax", "xml", xs@_*) => MergeStrategy.first
      case PathList("javax", "activation", xs@_*) => MergeStrategy.first
      case PathList("io", "netty", xs@_*) => MergeStrategy.first
      case PathList("org", "aopalliance", "intercept", xs@_*) => MergeStrategy.first
      case PathList("org", "aopalliance", "aop", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "spark", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "hadoop", xs@_*) => MergeStrategy.first
      case PathList("net", "jpountz", xs@_*) => MergeStrategy.last
      case PathList("net", "jcip", xs@_*) => MergeStrategy.first
      case PathList("javax", "inject", xs@_*) => MergeStrategy.first
      case PathList("javax", "annotation", xs@_*) => MergeStrategy.first
      case PathList("com", "sun", xs@_*) => MergeStrategy.first
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.first

      case x => (assemblyMergeStrategy in assembly).value.apply(x)
    },
    test in assembly := {},
    assemblyOutputPath in assembly := file(".") / "build" / s"${name.value}-${(version in ThisBuild).value}.jar",
    packageBin in Universal := {
      // Move sparkler-app & its dependencies to {Settings.buildDir}
      val fileMappings = (mappings in Universal).value
      val buildLocation = file(".") / Settings.buildDir / s"${name.value}-${(version in ThisBuild).value}"
      fileMappings foreach {
        case (file, name) => IO.copyFile(file, buildLocation / name)
      }

      // Move conf & bin to {Settings.buildDir}
      IO.copyDirectory(file(".") / Settings.confDir, file(".") / Settings.buildDir / Settings.confDir)
      IO.copyDirectory(file(".") / Settings.binDir, file(".") / Settings.buildDir / Settings.binDir)

      buildLocation
    },

  )
  .dependsOn(api)

lazy val testsBase = (project in file("sparkler-tests-base"))
  .settings(
    Settings.common,
    name := "sparkler-tests-base",
    libraryDependencies ++= Seq(
      Dependencies.Jetty.server,
      Dependencies.Jetty.servlet,
      Dependencies.jUnit,
      Dependencies.Slf4j.logback,
      Dependencies.Slf4j.api,
      Dependencies.Slf4j.log4j12,
    ),

  )
