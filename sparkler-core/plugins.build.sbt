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

lazy val api = ProjectRef(file("./"), "api") % "provided;test->test"// % "compile->compile;test->test"

lazy val sparklerPlugins = "sparkler-plugins"

lazy val plugins = (project in file(s"$sparklerPlugins"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.common,
    name := "sparkler-plugins"
  )
  .aggregate(
    fetcherChrome,
    fetcherHtmlUnit,
    fetcherJBrowser,
    scorerDdSvn,
    urlFilterRegex,
    urlFilterSameHost,
  )

/**
 * ================ PLUGINS ================
 */

// ------------- Template Plugin -------------

lazy val templatePlugin = (project in file(s"$sparklerPlugins/template-plugin"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "template-plugin",
    Settings.pluginManifest(
      id = "template-plugin",
      className = "edu.usc.irds.sparkler.plugin.MyPluginActivator",
      dependencies = List.empty
    )
  )
  .dependsOn(api)

// -------------------------------------------


// ---------------- Plugin Builds -------------

lazy val fetcherChrome = (project in file(s"$sparklerPlugins/fetcher-chrome"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "fetcher-chrome",
    libraryDependencies ++= Seq(
      //FetcherChrome.Selenium.chromeDriver,
      FetcherChrome.Selenium.java,
    ),
    Settings.pluginManifest(
      id = "fetcher-chrome",
      className = "edu.usc.irds.sparkler.plugin.FetcherChromeActivator",
      dependencies = List.empty
    ),
  )
  .dependsOn(api)

lazy val fetcherHtmlUnit = (project in file(s"$sparklerPlugins/fetcher-htmlunit"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "fetcher-htmlunit",
    libraryDependencies ++= Seq(
      FetcherHtmlUnit.htmlUnit,
    ),
    Settings.pluginManifest(
      id = "fetcher-htmlunit",
      className = "edu.usc.irds.sparkler.plugin.HtmlUnitFetcherActivator",
      dependencies = List.empty
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit,
      "--verbosity=1",
      "--run-listener=edu.usc.irds.sparkler.test.WebServerRunListener")
  )
  .dependsOn(api)

lazy val fetcherJBrowser = (project in file(s"$sparklerPlugins/fetcher-jbrowser"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "fetcher-jbrowser",
    libraryDependencies ++= Seq(
      FetcherJBrowser.jBrowser exclude ("org.slf4j", "slf4j-api"),
    ),
    Settings.pluginManifest(
      id = "fetcher-jbrowser",
      className = "edu.usc.irds.sparkler.plugin.FetcherJBrowserActivator",
      dependencies = List.empty
    )
  )
  .dependsOn(api)

lazy val scorerDdSvn = (project in file(s"$sparklerPlugins/scorer-dd-svn"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "scorer-dd-svn",
    libraryDependencies ++= Seq(
      ScorerDdSvn.httpClient
    ),
    Settings.pluginManifest(
      id = "scorer-dd-svn",
      className = "edu.usc.irds.sparkler.plugin.DdSvnScorerActivator",
      dependencies = List.empty
    )
  )
  .dependsOn(api)

lazy val urlFilterRegex = (project in file(s"$sparklerPlugins/urlfilter-regex"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "urlfilter-regex",
    Settings.pluginManifest(
      id = "urlfilter-regex",
      className = "edu.usc.irds.sparkler.plugin.RegexURLFilterActivator",
      dependencies = List.empty
    )
  )
  .dependsOn(api)

lazy val urlFilterSameHost = (project in file(s"$sparklerPlugins/urlfilter-samehost"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    Settings.plugin,
    name := "urlfilter-samehost",
    Settings.pluginManifest(
      id = "urlfilter-samehost",
      className = "edu.usc.irds.sparkler.plugin.UrlFilterSameHostActivator",
      dependencies = List.empty
    )
  )
  .dependsOn(api)
