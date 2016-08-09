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

package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;

import java.util.LinkedHashMap;

public class CommonUtil {

    public static LinkedHashMap getPluginConfiguration(SparklerConfiguration conf, String pluginId) throws SparklerException {
        if (conf.containsKey(Constants.key.PLUGINS)) {
            LinkedHashMap plugins = (LinkedHashMap) conf.get(Constants.key.PLUGINS);
            if (plugins.containsKey(pluginId))
                return (LinkedHashMap) plugins.get(pluginId);
            else
                throw new SparklerException("No configuration found for Plugin: " + pluginId);
        }
        else
            throw new SparklerException("No plugin configuration found!");
    }

}
