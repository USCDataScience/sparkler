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

import java.nio.file.Files
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.archetypes.scripts.BatStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import sbt.{Def, File, _}
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._

import scala.collection.JavaConverters._

object Settings {
  lazy val projectOrganization = "edu.usc.irds.sparkler"
  lazy val projectMaintainer = "irds-l@mymaillists.usc.edu"
  lazy val confDir = "conf"
  lazy val binDir = "bin"
  lazy val buildDir = "build"
  lazy val pluginsDir = "plugins"
  lazy val cmdAlias = addCommandAlias(
    "package", "universal:packageBin"
  ) ++ addCommandAlias(
    "releaseSilent", "release with-defaults"
  )
  lazy val common = cmdAlias ++ Seq(
    maintainer in Universal := projectMaintainer,
    publish / skip := true,
    makeBatScripts := Seq(),
    makeBashScripts := Seq(),
    scalacOptions ++=  Seq(
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation",
      "-encoding",
      "utf8"
    ),
    resolvers ++= Seq(
      "Maven Releases" at "https://repo1.maven.org/maven2/",
      "Typesafe Releases" at "https://repo.typesafe.com/typesafe/ivy-releases/",
      "JBoss Repository" at "https://repository.jboss.org/nexus/content/repositories/",
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Scala-Tools Snapshots" at "https://scala-tools.org/repo-snapshots/",
      "Gitlab Spicule 2" at "https://gitlab.com/api/v4/projects/26391218/packages/maven",
      "Gitlab Spicule" at "https://gitlab.com/api/v4/projects/23300400/packages/maven",
      "Private Github" at "https://gitlab.com/api/v4/projects/28025579/packages/maven",
      "Ashot Gitlab" at "https://gitlab.com/api/v4/projects/28250236/packages/maven"
    )


  )
  lazy val assemblyProject: Seq[Def.Setting[_]] = common ++ baseAssemblySettings ++ Seq(
    test in assembly := {},
    mappings in Universal := {
      val universalMappings = (mappings in Universal).value
      val fatJar = (assembly in Compile).value
      universalMappings :+ (fatJar -> ("lib/" + fatJar.getName))
    }
  )
  lazy val plugin: Seq[Def.Setting[_]] = assemblyProject ++ Seq(
    autoScalaLibrary := false,
    assemblyMergeStrategy in assembly := {
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.last
      case x if x.contains("Log4j2Plugins.dat") => MergeStrategy.first
      case x if x.contains("module-info.class") => MergeStrategy.first
      case x if x.contains("jetty-dir.css") => MergeStrategy.first
      case x if x.contains("public-suffix-list.txt") => MergeStrategy.first
      case x if x.contains("bus-extensions.txt") => MergeStrategy.first
      case x if x.contains("blueprint.handlers") => MergeStrategy.first
      case x if x.contains("git.properties") => MergeStrategy.first
      case x if x.contains("config.fmpp") => MergeStrategy.first
      case x if x.contains("META-INF/versions/9/javax/xml/bind/") => MergeStrategy.first
      case x if x.contains("MANIFEST.MF") => MergeStrategy.discard
      case x if x.contains("ExtensionModule") => MergeStrategy.first
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("javax", "activation", xs@_*) => MergeStrategy.first
      case PathList("javax", "inject", xs@_*) => MergeStrategy.first
      case PathList("javax", "xml", xs@_*) => MergeStrategy.first
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
      case PathList("com", "sun", xs@_*) => MergeStrategy.first
      case PathList("org", "aopalliance", xs@_*) => MergeStrategy.first

      case PathList("org", "apache", "spark", "unused", xs@_*) => MergeStrategy.first
      case PathList("org", "apache", "spark", xs@_*) => MergeStrategy.discard

      case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.first
      case PathList("org", "slf4j", "impl", xs@_*) => MergeStrategy.first
      //case PathList("io", "netty", xs@_*) => MergeStrategy.last

      case x => (assemblyMergeStrategy in assembly).value.apply(x)
    },
      assemblyOutputPath in assembly := file(".") / buildDir / pluginsDir / s"${name.value}-${(version in ThisBuild).value}.jar"
  )

  def pluginManifest(id: String, className: String,
                     dependencies: List[String]): Seq[Def.Setting[Task[Seq[PackageOption]]]] = {
    Seq(
      packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
        "Plugin-Id" -> id,
        "Plugin-Class" -> className,
        "Plugin-Version" -> (version in ThisBuild).value,
        "Plugin-Provider" -> projectOrganization,
        "Plugin-Dependencies" -> dependencies.mkString(",")
      )
    )
  }

  /**
   * Merge files from all directories into one
   * If there are duplicates, keep file from the last directory
   * @param dirs to merge
   * @return sequence of (File to include in package, Path in package)
   */
  def mergeDirs(dirs: File *): Seq[(File, String)] = {
    var destinationMap: Map[String, File] = Map.empty
    for (dir <- dirs) {
      val dirPath = dir.toPath
      Files
        .walk(dirPath)
        .iterator()
        .asScala
        .foreach(p => {
          destinationMap += (dirPath.relativize(p).toString -> p.toFile)
        })
    }
    destinationMap
      .toSeq
      .map(entry => (entry._2, entry._1))
  }
}