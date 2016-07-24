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

import org.apache.hadoop.conf.Configuration;

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
    }


    abstract class defaults {
        /**
         * Create configuration instance for Sparkler
         */
        public static Configuration newDefaultConfig(){

            Configuration conf = new Configuration();
            conf.set(key.UUID_KEY, UUID.randomUUID().toString());
            conf.addResource(file.SPARKLER_DEFAULT);
            conf.addResource(file.SPARKLER_SITE);
            return conf;
        }

    }


    interface file {
        String SPARKLER_DEFAULT = "sparkler-default.xml";
        String SPARKLER_SITE = "sparkler-site.xml";
        String CONF_DIR = "conf";
    }

}
