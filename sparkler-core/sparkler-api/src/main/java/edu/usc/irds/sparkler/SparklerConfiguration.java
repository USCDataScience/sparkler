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

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparklerConfiguration extends JSONObject {

    public SparklerConfiguration() {
        super();
    }

    public SparklerConfiguration(Map<?, ?> map) {
        super(map);
    }

    public Map<String, Object> getPluginConfiguration(String pluginId) throws SparklerException {
        pluginId = pluginId.replace("-", ".");
        if (this.containsKey(Constants.key.PLUGINS)) {
            Map plugins = (Map) this.get(Constants.key.PLUGINS);
            if (plugins.containsKey(pluginId)) {
                return (Map<String, Object>) plugins.get(pluginId);
            } else {
                String[] parts = pluginId.split(":");
                if (parts.length >= 3) { // groupId:artifactId:version
                    // first check without version
                    String newId = parts[0] + ":" + parts[1];
                    if (plugins.containsKey(newId)) {
                        return (Map<String, Object>) plugins.get(newId);
                    } else if (plugins.containsKey(parts[1])) { // just the id, no groupId or version
                        return (Map<String, Object>) plugins.get(parts[1]);
                    }
                }
                throw new SparklerException("No configuration found for Plugin: " + pluginId);
            }
        } else {
            throw new SparklerException("No plugin configuration found!");
        }
    }

    public void overloadConfig(String object) {
      if(object != null && !object.equals("") && !object.equals(" ")){
        JSONParser parser = new JSONParser();
        JSONObject json = null;
            try{
                json = (JSONObject) parser.parse(object);
            } catch (Exception exception){
                System.out.println("Error parsing overload json: " + exception+ " for json: "+object);
                System.exit(0); // ???
            }

            HashMap<String, Object> yourHashMap = new Gson().fromJson(json.toString(), HashMap.class);
            Map o = deepMerge(this, yourHashMap);
            JSONObject j = new JSONObject(this);
            String str = j.toJSONString();
            System.out.println(str);
        }
    }

    private static Map deepMerge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = (Map) original.get(key);
                Map newChild = (Map) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
                List originalChild = (List) original.get(key);
                List newChild = (List) newMap.get(key);
                for (Object each : newChild) {
                    if (!originalChild.contains(each)) {
                        originalChild.add(each);
                    }
                }
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }

    public String getDatabaseURI() {
        String dbToUse = (String) this.getOrDefault(Constants.key.CRAWLDB_BACKEND, "solr"); // solr is default
        return (String) this.get(dbToUse+".uri");
    }

}
