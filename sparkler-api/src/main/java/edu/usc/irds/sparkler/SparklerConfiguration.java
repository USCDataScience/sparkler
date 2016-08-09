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

package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.model.PluginModel;

import java.io.Serializable;

public class SparklerConfiguration implements Serializable {

    String uuid;
    String crawldbUri;
    String sparkMaster;
    Integer generateTopN = 1024;
    Integer generateTopGroups = 256;
    Long fetcherServerDelay = 1000L;
    PluginModel plugins;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCrawldbUri() {
        return crawldbUri;
    }

    public void setCrawldbUri(String crawldbUri) {
        this.crawldbUri = crawldbUri;
    }

    public String getSparkMaster() {
        return sparkMaster;
    }

    public void setSparkMaster(String sparkMaster) {
        this.sparkMaster = sparkMaster;
    }

    public Integer getGenerateTopN() {
        return generateTopN;
    }

    public void setGenerateTopN(Integer generateTopN) {
        this.generateTopN = generateTopN;
    }

    public Integer getGenerateTopGroups() {
        return generateTopGroups;
    }

    public void setGenerateTopGroups(Integer generateTopGroups) {
        this.generateTopGroups = generateTopGroups;
    }

    public Long getFetcherServerDelay() {
        return fetcherServerDelay;
    }

    public void setFetcherServerDelay(Long fetcherServerDelay) {
        this.fetcherServerDelay = fetcherServerDelay;
    }

    public PluginModel getPlugins() {
        return plugins;
    }

    public void setPlugins(PluginModel plugins) {
        this.plugins = plugins;
    }

    public void mask(SparklerConfiguration other) {
        this.crawldbUri = other.crawldbUri == null ? this.crawldbUri : other.crawldbUri;
        this.sparkMaster = other.sparkMaster == null ? this.sparkMaster : other.sparkMaster;
        this.generateTopN = other.generateTopN == null ? this.generateTopN : other.generateTopN;
        this.generateTopGroups = other.generateTopGroups == null ? this.generateTopGroups : other.generateTopGroups;
        this.fetcherServerDelay = other.fetcherServerDelay == null ? this.fetcherServerDelay : other.fetcherServerDelay;
        if (other.plugins != null) this.plugins.mask(other.plugins);
    }
}
