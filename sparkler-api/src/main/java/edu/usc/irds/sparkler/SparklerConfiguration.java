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
    @NotNull
    boolean kafkaEnable;

    @NotNull
    int generateTopn;

    @NotNull
    String sparkMaster;

    @NotNull
    @URL
    String crawlDBURI;

    @NotNull
    @Min(1)
    int generateTopGroups;

    @NotNull
    String kafkaTopic;

    @NotNull
    @Min(200)
    int fetcherServerDelay;

    @NotNull
    String pluginsBundleDirectory;

    @NotNull
    String kafkaListeners;

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
