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

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * A static container for all the constants
 * (NOTE: interface is used to make all the fields as public static final )
 */
public interface Constants {

    interface key { //config key name

        //ID for config
        @ConfigKey
        String UUID_KEY = "sparkler.conf.uuid";

        // General Properties
        @ConfigKey
        String CRAWLDB = "crawldb.uri";

        // Apache Spark Properties
        @ConfigKey
        String SPARK_MASTER = "spark.master";

        // Apache Kafka Properties
        @ConfigKey
        String KAFKA_ENABLE = "kafka.enable";

        @ConfigKey
        String KAFKA_LISTENERS = "kafka.listeners";

        @ConfigKey
        String KAFKA_TOPIC = "kafka.topic";

        // HTTP Properties

        // Database Properties

        // Generator Properties
        @ConfigKey(type = int.class)
        String GENERATE_TOPN = "generate.topn";

        @ConfigKey(type = int.class)
        String GENERATE_TOP_GROUPS = "generate.top.groups";

        // Fetcher Properties
        @ConfigKey(type = int.class)
        String FETCHER_SERVER_DELAY = "fetcher.server.delay";

        // Parser Properties

        // Plugin Properties
        @ConfigKey
        String PLUGINS_BUNDLE_DIRECTORY = "plugins.bundle.directory";

        @ConfigKey
        String PLUGINS = "plugins";

        @ConfigKey
        String ACTIVE_PLUGINS = "plugins.active";
    }


    abstract class defaults {
        /**
         * Create configuration instance for Sparkler
         */
        public static SparklerConfiguration newDefaultConfig() {
            Yaml yaml = new Yaml();
            InputStream defaultConfig = null;
            InputStream overrideConfig = null;
            SparklerConfiguration sparklerConf = null;
            try {
                String temp = file.SPARKLER_DEFAULT;
                defaultConfig = Constants.class.getClassLoader().getResourceAsStream(file.SPARKLER_DEFAULT);
                overrideConfig = Constants.class.getClassLoader().getResourceAsStream(file.SPARKLER_SITE);
                Map<String, Object> defaultConfigMap = (Map<String, Object>) yaml.load(defaultConfig);
                Map<String, Object> overrideConfigMap = (Map<String, Object>) yaml.load(overrideConfig);
                sparklerConf = new SparklerConfiguration(overrideConfigData(defaultConfigMap, overrideConfigMap));
            } catch (Exception e) {
                e.printStackTrace();
                IOUtils.closeQuietly(defaultConfig);
                IOUtils.closeQuietly(overrideConfig);
                System.exit(1);
            } finally {
                IOUtils.closeQuietly(defaultConfig);
                IOUtils.closeQuietly(overrideConfig);
            }

            if (sparklerConf != null) {
                sparklerConf.put(key.UUID_KEY, UUID.randomUUID().toString());
            }

            return sparklerConf;
        }

        /**
         * @param defaultConfig
         * @param overrideConfig
         * @return Config setup for the system to run.
         * @note This function is responsible for setting up the config for the system.
         * If SPARKLER_SITE has some override properties defined then those need to be
         * overridden in the default config. This function does that precisely.
         */
        public static Map<String, Object> overrideConfigData(Map<String, Object> defaultConfig, Map<String, Object> overrideConfig) {
            if (overrideConfig == null) {
                //overrideConfig was not provided going with default then.
                return defaultConfig;
            }
            //Iterating over pair in map and overriding it with new config
            Iterator iterator = defaultConfig.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                //Override the config parameter
                if (overrideConfig.containsKey(pair.getKey().toString())) {
                    defaultConfig.put(pair.getKey().toString(), overrideConfig.get(pair.getKey()));
                }
            }
            return defaultConfig;
        }
    }


    interface file {
        String SPARKLER_DEFAULT = "sparkler-default.yaml";
        String SPARKLER_SITE = "sparkler-site.yaml";
        String CONF_DIR = "conf";

        /**
         * Apache Felix Framework Factory META file
         */
        String FELIX_FRAMEWORK_FACTORY = "META-INF/services/org.osgi.framework.launch.FrameworkFactory";
        /**
         * Specifying Apache Felix bundle directory.
         * TODO:Should come from Sparkler Config
         **/
        //String FELIX_BUNDLE_DIR = key.PLUGINS_BUNDLE_DIRECTORY;
        //String FELIX_BUNDLE_DIR = "../bundles";
        //String FELIX_BUNDLE_DIR = "/Users/karanjeetsingh/git_workspace/madhav-sparkler/sparkler-app/bundles";


        /**
         * Apache Felix configuration properties file
         * TODO:Should come from Sparkler Config
         */
        String FELIX_CONFIG = "felix-config.properties";

    }


    interface solr { // Solr Fields
        String ID = "id";
        String CRAWL_ID = "crawl_id";
        String URL = "url";
        String GROUP = "group";
        String FETCH_TIMESTAMP = "fetch_timestamp";
        String RETRIES_SINCE_FETCH = "retries_since_fetch";
        String NUM_FETCHES = "numFetches";
        String DISCOVER_DEPTH = "discover_depth";
        String FETCH_DEPTH = "fetch_depth";
        String SCORE = "score";
        String STATUS = "status";
        String LAST_UPDATED_AT = "last_updated_at";
        String EXTRACTED_TEXT = "extracted_text";
        String CONTENT_TYPE = "content_type";
        String FETCH_STATUS_CODE = "fetch_status_code";
        String SIGNATURE = "signature";
        String OUTLINKS = "outlinks";
        String RELATIVE_PATH = "relative_path";
        String DEDUPE_ID = "dedupe_id";
        String MD_SUFFIX = "_md";
    }

}
