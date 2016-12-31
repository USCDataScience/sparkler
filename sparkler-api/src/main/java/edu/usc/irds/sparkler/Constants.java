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

        // Fetcher Properties
        @ConfigKey(type = int.class)
        String FETCHER_SERVER_DELAY = "fetcher.server.delay";

        // Parser Properties

        // Plugin Properties
        @ConfigKey
        String PLUGINS_BUNDLE_DIRECTORY = "plugins.bundle.directory";

        @ConfigKey
        String PLUGINS = "plugins";
    }


    abstract class defaults {
        /**
         * Create configuration instance for Sparkler
         */
        public static SparklerConfiguration newDefaultConfig(){
            Yaml yaml = new Yaml();
            InputStream input = null;
            SparklerConfiguration sparklerConf = null;
            try {
                input = Constants.class.getClassLoader().getResourceAsStream(file.SPARKLER_DEFAULT);
                Map<String,Object> yamlMap = (Map<String, Object>) yaml.load(input);
                sparklerConf = new SparklerConfiguration(yamlMap);

                //input = Constants.class.getClassLoader().getResourceAsStream(file.SPARKLER_SITE);
                //if(sparklerSite != null)
                //    sparklerConf.mask(sparklerSite);
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
         * TODO:Should come from Sparler Config
         */
        String FELIX_CONFIG = "felix-config.properties";

    }


    interface solr { // Solr Fields
        String ID = "id";
        String JOBID = "jobId";
        String URL = "url";
        String GROUP = "group";
        String GROUP_ID = "group_id";
        String LAST_FETCHED_AT = "lastFetchedAt";
        String NUM_TRIES = "numTries";
        String NUM_FETCHES = "numFetches";
        String DEPTH = "depth";
        String SCORE = "score";
        String STATUS = "status";
        String LAST_UPDATED_AT = "lastUpdatedAt";
        String PLAIN_TEXT = "plainText";
        String MD_SUFFIX = "_md";
    }

}
