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

import edu.usc.irds.sparkler.util.HibernateConstraints.AnotherFieldTrue.AnotherFieldTrue;
import edu.usc.irds.sparkler.util.HibernateConstraints.IsDirectory.IsDirectory;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.json.simple.JSONObject;

import javax.validation.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@AnotherFieldTrue.List({@AnotherFieldTrue(fieldMethod = "getKafkaProperties", dependFieldTrueMethod = "isKafkaEnable", message = "kafka properties should be specified"),
        @AnotherFieldTrue(fieldMethod = "getUrlFiltersRegexProperties", dependFieldTrueMethod = "isUrlfilterRegexEnable", message = "url filter properties should be specified"),
        @AnotherFieldTrue(fieldMethod = "getjBrowserProperties", dependFieldTrueMethod = "isFetcherJBrowserEnable", message = "jBrowser properties should be specified")
})
public class SparklerConfiguration extends JSONObject {
    /*********************
     * Solr Related Config
     ********************/
    @NotNull(message = "crawldb.uri needs to be provided")
    @URL(message = "crawldb.uri is not a valid URL")
    private String crawlDBURI;

    /*********************
     * Apache Spark Config
     ********************/
    @NotEmpty(message = "spark.master needs to be provided")
    private String sparkMaster;


    /*********************
     * Generate Properties
     ********************/
    @NotNull(message = "generate.topn needs to be provided")
    @Min(value = 1, message = "generate.topn should be at least 1")
    private int generateTopn;

    @NotNull(message = "generate.top.groups needs to be provided")
    @Min(value = 1, message = "generate.top.groups should be at least 1")
    private int generateTopGroups;

    /***************************
     * Fetcher Server Properties
     **************************/
    @NotNull(message = "fetcher.server.delay needs to be provided")
    @Min(value = 0, message = "fetcher.server.delay should be non-negative")
    private int fetcherServerDelay;

    /****************
     * Plugins Config
     ***************/
    @NotEmpty(message = "plugins.bundle.directory should be provided")
    @IsDirectory(message = "plugins.bundle.directory is not a valid directory")
    private String pluginsBundleDirectory;

    @NotNull(message = "plugins.active should be provided")
    private ArrayList<String> pluginsActive;

    /*********************
     * Apache Kafka Config
     ********************/
    boolean kafkaEnable;

    /**
     * @apiNote This class helps in making config variables for kafka
     * plugin in sparkler.
     */
    class KafkaProperties {
        @NotEmpty(message = "kafka.topic should be provided")
        String kafkaTopic;

        @NotEmpty(message = "kafka.listeners should be provided")
        @URL(message = "kafka.listeners should be valid URL")
        String kafkaListeners;

        //Getters and Setters
        public String getKafkaTopic() {
            return kafkaTopic;
        }

        public void setKafkaTopic(String kafkaTopic) {
            this.kafkaTopic = kafkaTopic;
        }

        public String getKafkaListeners() {
            return kafkaListeners;
        }

        public void setKafkaListeners(String kafkaListeners) {
            this.kafkaListeners = kafkaListeners;
        }
    }

    @Valid
    KafkaProperties kafkaProperties;

    //URL Filter Properties
    boolean urlfilterRegexEnable;

    /**
     * @apiNote This class is responsible for storing config properties of
     * URL filters. Any change related to plugin should be updated
     * here.
     */
    private class UrlFiltersRegexProperties {
        //TODO: IsFile annotation should be added in this.
        @NotEmpty(message = "urlfilter.regex.file should be provided")
        String urlfilterRegexFile;

        public String getUrlfilterRegexFile() {
            return urlfilterRegexFile;
        }

        public void setUrlfilterRegexFile(String urlfilterRegexFile) {
            this.urlfilterRegexFile = urlfilterRegexFile;
        }
    }

    @Valid
    private UrlFiltersRegexProperties urlFiltersRegexProperties;

    //fetcher jbrowser properties
    boolean fetcherJBrowserEnable;

    /**
     * @apiNote This class is responsible for maintaining configs of JBrowser
     * plugin. Any updates to JBrowser config should be updated here.
     */
    class JBrowserProperties {

        @NotNull(message = "socket.timeout should be provided")
        @Min(value = 0, message = "socket.timeout should be non-negative")
        int socketTimeout;

        @NotNull(message = "connect.timeout should be provided")
        @Min(value = 0, message = "connect.timeout should be non-negative")
        int connectTimeout;

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
    }

    @Valid
    JBrowserProperties jBrowserProperties;

    //Validator Instance of Hibernate
    private Validator validator;

    public void validateConfigs() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        Set<ConstraintViolation<SparklerConfiguration>> constraintViolations = validator.validate(this);
        Iterator it = constraintViolations.iterator();
        StringBuilder st = new StringBuilder();
        while (it.hasNext()) {
            ConstraintViolation constraintViolation = (ConstraintViolation) it.next();
            st.append(constraintViolation.getMessage() + "\n");
        }
        if (!constraintViolations.isEmpty()) {
            throw new RuntimeException(st.toString());
        }
    }

    //TODO: Move things to getters and setters

    //QUESTION: Why is this even required? Is it because of JSONObject
    //Extension? If yes, then that should be removed once we are on getters
    //and setters or else it should be removed now as it is of no use.
    public SparklerConfiguration() {
        super();
        throw new RuntimeException("Configuration file not provided");
    }

    //Constructor initializing the config.
    public SparklerConfiguration(Map<?, ?> map) {
        super(map);
        //CrawlDB reads
        boolean errorFound = false;
        StringBuilder errorMessage = new StringBuilder();
        try {
            this.crawlDBURI = map.get("crawldb.uri").toString();
        } catch (NullPointerException e) {
            errorMessage.append("crawldb.uri should be provided\n");
            errorFound = true;
        }
        //Spark Reads
        try {
            this.sparkMaster = map.get("spark.master").toString();
        } catch (NullPointerException e) {
            errorMessage.append("spark.master should be provided\n");
            errorFound = true;
        }
        //generate properties reads
        try {
            this.generateTopn = Integer.parseInt(map.get("generate.topn").toString());
        } catch (Exception e) {
            errorMessage.append("generate.topn is not a valid Integer value\n");
            errorFound = true;
        }
        try {
            this.generateTopGroups = Integer.parseInt(map.get("generate.top.groups").toString());
        } catch (Exception e) {
            errorMessage.append("generate.top.groups is not a valid Integer value\n");
            errorFound = true;
        }
        //fetcher reads
        try {
            this.fetcherServerDelay = Integer.parseInt(map.get("fetcher.server.delay").toString());
        } catch (Exception e) {
            errorMessage.append("fetcher.server.delay is not a valid Integer value\n");
            errorFound = true;
        }
        //Plugins List Read
        try {
            pluginsBundleDirectory = map.get("plugins.bundle.directory").toString();
        } catch (NullPointerException e) {
            errorMessage.append("plugins.bundle.directory should be provided\n");
        }
        try {
            this.pluginsActive = (ArrayList<String>) map.get("plugins.active");
        } catch (Exception e) {
            errorMessage.append("plugins.active is not a valid list of plugins\n");
            errorFound = true;
        }

        //Kafka Reads
        try {
            this.kafkaEnable = Boolean.parseBoolean(map.get("kafka.enable").toString());
        } catch (Exception e) {
            errorMessage.append("kafka.enable should be specified\n");
            errorFound = true;
        }
        if (kafkaEnable) {
            try {
                this.kafkaProperties = new KafkaProperties();
                this.kafkaProperties.setKafkaListeners(map.get("kafka.listeners").toString());
                this.kafkaProperties.setKafkaTopic(map.get("kafka.topic").toString());
            } catch (Exception e) {
                errorFound = true;
                errorMessage.append("Kafka Properties are not valid\n");
            }
        }
        if (this.pluginsActive != null) {
            //Plugins Map
            Map<String, Object> pluginsMap = (Map<String, Object>) map.get("plugins");
            //URLFilter Reads
            this.urlfilterRegexEnable = this.pluginsActive.contains("urlfilter.regex");
            if (this.urlfilterRegexEnable) {
                try {
                    Map<String, Object> urlFilterMap = (Map<String, Object>) pluginsMap.get("urlfilter.regex");
                    this.urlFiltersRegexProperties = new UrlFiltersRegexProperties();
                    this.urlFiltersRegexProperties.setUrlfilterRegexFile(urlFilterMap.get("urlfilter.regex.file").toString());
                } catch (Exception e) {
                    errorFound = true;
                    errorMessage.append("plugins->urlfilter.regex property is not valid\n");
                }
            }

            //JBrowser Reads
            this.fetcherJBrowserEnable = this.pluginsActive.contains("fetcher.jbrowser");
            if (this.fetcherJBrowserEnable) {
                try {
                    Map<String, Object> fetcherjbrowserMap = (Map<String, Object>) pluginsMap.get("fetcher.jbrowser");
                    this.jBrowserProperties = new JBrowserProperties();
                    this.jBrowserProperties.setSocketTimeout(Integer.parseInt(fetcherjbrowserMap.get("socket.timeout").toString()));
                    this.jBrowserProperties.setConnectTimeout(Integer.parseInt(fetcherjbrowserMap.get("connect.timeout").toString()));
                } catch (Exception e) {
                    errorFound = true;
                    errorMessage.append("plugins->fetcher.jbrowser property not valid\n");
                }
            }
        } else {
            errorFound = true;
            errorMessage.append("plugins.active is an empty list\n");
        }
        //Some errors found in config
        if (errorFound) {
            throw new RuntimeException(errorMessage.toString());
        } else {
            //config properties are mentioned. Now going for validation.
            this.validateConfigs();
        }
    }


    public LinkedHashMap<String, Object> getPluginConfiguration(String pluginId) throws SparklerException {

        if (this.containsKey(Constants.key.PLUGINS)) {
            LinkedHashMap plugins = (LinkedHashMap) this.get(Constants.key.PLUGINS);
            if (plugins.containsKey(pluginId)) {
                return (LinkedHashMap<String, Object>) plugins.get(pluginId);
            } else {
                throw new SparklerException("No configuration found for Plugin: " + pluginId);
            }
        } else {
            throw new SparklerException("No plugin configuration found!");
        }
    }

    public String getCrawlDBURI() {
        return crawlDBURI;
    }

    public void setCrawlDBURI(String crawlDBURI) {
        this.crawlDBURI = crawlDBURI;
    }

    public String getSparkMaster() {
        return sparkMaster;
    }

    public void setSparkMaster(String sparkMaster) {
        this.sparkMaster = sparkMaster;
    }

    public int getGenerateTopn() {
        return generateTopn;
    }

    public void setGenerateTopn(int generateTopn) {
        this.generateTopn = generateTopn;
    }

    public int getGenerateTopGroups() {
        return generateTopGroups;
    }

    public void setGenerateTopGroups(int generateTopGroups) {
        this.generateTopGroups = generateTopGroups;
    }

    public int getFetcherServerDelay() {
        return fetcherServerDelay;
    }

    public void setFetcherServerDelay(int fetcherServerDelay) {
        this.fetcherServerDelay = fetcherServerDelay;
    }

    public String getPluginsBundleDirectory() {
        return pluginsBundleDirectory;
    }

    public void setPluginsBundleDirectory(String pluginsBundleDirectory) {
        this.pluginsBundleDirectory = pluginsBundleDirectory;
    }

    public ArrayList<String> getPluginsActive() {
        return pluginsActive;
    }

    public void setPluginsActive(ArrayList<String> pluginsActive) {
        this.pluginsActive = pluginsActive;
    }

    public boolean isKafkaEnable() {
        return kafkaEnable;
    }

    public void setKafkaEnable(boolean kafkaEnable) {
        this.kafkaEnable = kafkaEnable;
    }

    public KafkaProperties getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public boolean isUrlfilterRegexEnable() {
        return urlfilterRegexEnable;
    }

    public void setUrlfilterRegexEnable(boolean urlfilterRegexEnable) {
        this.urlfilterRegexEnable = urlfilterRegexEnable;
    }

    public UrlFiltersRegexProperties getUrlFiltersRegexProperties() {
        return urlFiltersRegexProperties;
    }

    public void setUrlFiltersRegexProperties(UrlFiltersRegexProperties urlFiltersRegexProperties) {
        this.urlFiltersRegexProperties = urlFiltersRegexProperties;
    }

    public boolean isFetcherJBrowserEnable() {
        return fetcherJBrowserEnable;
    }

    public void setFetcherJBrowserEnable(boolean fetcherJBrowserEnable) {
        this.fetcherJBrowserEnable = fetcherJBrowserEnable;
    }

    public JBrowserProperties getjBrowserProperties() {
        return jBrowserProperties;
    }

    public void setjBrowserProperties(JBrowserProperties jBrowserProperties) {
        this.jBrowserProperties = jBrowserProperties;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
