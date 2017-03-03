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

import edu.usc.irds.sparkler.util.HibernateConstraints.Directory.IsDirectory;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.json.simple.JSONObject;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class SparklerConfiguration extends JSONObject {
    /*********************
     * Solr Related Config
     ********************/
    @NotNull(message = "crawldb.uri needs to be provided")
    @URL(message = "crawldb.uri is not a valid URL")
    String crawlDBURI;

    /*********************
     * Apache Spark Config
     ********************/
    @NotEmpty(message = "spark.master needs to be provided")
    String sparkMaster;

    /*********************
     * Apache Kafka Config
     ********************/
    @NotNull(message = "kafka.enable should be set to true or false")
    boolean kafkaEnable;

    @NotEmpty
    String kafkaTopic;

    @NotEmpty
    String kafkaListeners;

    /*********************
     * Generate Properties
     ********************/
    @NotNull(message = "generate.topn needs to be provided")
    @Min(value = 1, message = "generate.topn should be at least 1")
    int generateTopn;

    @NotNull(message = "generate.top.groups needs to be provided")
    @Min(value = 1, message = "generate.top.groups should be at least 1")
    int generateTopGroups;

    /***************************
     * Fetcher Server Properties
     **************************/
    @NotNull(message = "fetcher.server.delay needs to be provided")
    @Min(value = 200, message = "fetcher.server.delay should be at least 200 to maintain politeness")
    int fetcherServerDelay;

    /****************
     * Plugins Config
     ***************/
    @NotEmpty(message = "plugins.bundle.directory should be provided")
    @IsDirectory(message = "not a directory")
    String pluginsBundleDirectory;


    private static Validator validator;

    public SparklerConfiguration() {
        super();
    }

    public SparklerConfiguration(Map<?, ?> map) {
        super(map);
        crawlDBURI = map.get("crawldb.uri").toString();
    }

    public void validateConfigs() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        System.out.println(this);
        Set<ConstraintViolation<SparklerConfiguration>> constraintViolations = validator.validate(this);
        System.out.println(constraintViolations.size());
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
}
