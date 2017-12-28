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

import java.io.InputStream;
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

        @ConfigKey
        String GENERATE_SORTBY = "generate.sortby";

        @ConfigKey
        String GENERATE_GROUPBY = "generate.groupby";

        // Fetcher Properties
        @ConfigKey(type = int.class)
        String FETCHER_SERVER_DELAY = "fetcher.server.delay";

        @ConfigKey
        String PLUGINS = "plugins";

        @ConfigKey
        String ACTIVE_PLUGINS = "plugins.active";

        @ConfigKey
        String FETCHER_HEADERS = "fetcher.headers";

        @ConfigKey
        String FETCHER_USER_AGENTS = "fetcher.user.agents";
    }


    abstract class defaults {
        /**
         * Create configuration instance for Sparkler
         */
        public static SparklerConfiguration newDefaultConfig(){
            //FIXME: needs rework!
            Yaml yaml = new Yaml();
            InputStream input = null;
            SparklerConfiguration sparklerConf = null;
            try {
                input = Constants.class.getClassLoader().getResourceAsStream(file.SPARKLER_DEFAULT);
                Map<String,Object> yamlMap = (Map<String, Object>) yaml.load(input);
                sparklerConf = new SparklerConfiguration(yamlMap);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(input);
            }

            if (sparklerConf != null) {
                sparklerConf.put(key.UUID_KEY, UUID.randomUUID().toString());
            }
            return sparklerConf;
        }
    }

    interface file {
        String SPARKLER_DEFAULT = "sparkler-default.yaml";
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
        String SCORE = "page_score";
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
        String HDR_SUFFIX = "_hd";
        String RESPONSE_TIME = "response_time";
    }

}
