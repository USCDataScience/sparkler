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

import org.json.simple.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class SparklerConfiguration extends JSONObject {

    public SparklerConfiguration() {
        super();
    }

    public SparklerConfiguration(Map<?, ?> map) {
        super(map);
    }

    public LinkedHashMap<String,Object> getPluginConfiguration(String pluginId) throws SparklerException {
        pluginId = pluginId.replace("-", ".");
        if (this.containsKey(Constants.key.PLUGINS)) {
            LinkedHashMap plugins = (LinkedHashMap) this.get(Constants.key.PLUGINS);
            if (plugins.containsKey(pluginId)) {
                return (LinkedHashMap<String, Object>) plugins.get(pluginId);
            } else {
                String[] parts = pluginId.split(":");
                if (parts.length >= 3){ // groupId:artifactId:version
                    //first check without version
                    String newId = parts[0] + ":" + parts[1];
                    if (plugins.containsKey(newId)) {
                        return (LinkedHashMap<String, Object>) plugins.get(newId);
                    } else if (plugins.containsKey(parts[1])){ // just the id, no groupId or version
                        return (LinkedHashMap<String, Object>) plugins.get(parts[1]);
                    }
                }
                throw new SparklerException("No configuration found for Plugin: " + pluginId);
            }
        } else {
            throw new SparklerException("No plugin configuration found!");
        }
    }
}
