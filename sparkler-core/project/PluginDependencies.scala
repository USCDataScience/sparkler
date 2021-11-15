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

// Define global plugin dependencies here
object PluginDependencies {}

object FetcherChrome {
  object Selenium {
    private val group = "org.seleniumhq.selenium"
    private val version = "3.141.59"
    lazy val chromeDriver = group % "selenium-chrome-driver" % version
    lazy val java = group % "selenium-java" % version
  }
  lazy val browserup = "com.browserup" % "browserup-proxy-core" % "3.0.0-SNAPSHOT"
  lazy val seleniumscripter = "uk.co.spicule" % "seleniumscripter" % "1.7.9"
  lazy val magnesium_script = "uk.co.spicule" % "magnesium-script" % "0.2.0"
}

object FetcherHtmlUnit {
  lazy val htmlUnit = "net.sourceforge.htmlunit" % "htmlunit" % "2.26"
}

object FetcherJBrowser {
  lazy val jBrowser = "com.machinepublishers" % "jbrowserdriver" % "0.16.4"
}

object ScorerDdSvn {
  lazy val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6"
}

object Databricks {
  lazy val wrapper = "com.kytheralabs" % "webcrawlerwrapper_2.12" % "0.1-SNAPSHOT"
}
