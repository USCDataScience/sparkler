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

package edu.usc.irds.sparkler.plugin;


import edu.usc.irds.sparkler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;


public class DdSvnScorer extends AbstractExtensionPoint implements Scorer {

    private final static Logger LOG = LoggerFactory.getLogger(DdSvnScorer.class);

    private final static String SCORE_KEY = "svn_score";

    @ConfigKey
    public static final String SCORER_DD_SVN_URL = "scorer.dd.svn.url";

    public DdSvnScorer() {

    }

    @Override
    public void init(JobContext context) throws SparklerException {
        super.init(context);
        SparklerConfiguration config = jobContext.getConfiguration();
        LinkedHashMap pluginConfig = config.getPluginConfiguration(pluginId);
    }

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        this.pluginId = pluginId;
        init(context);
    }

    @Override
    public String getScoreKey() {
        return SCORE_KEY;
    }

    @Override
    public Double score(byte[] content) throws Exception {
        //TODO
        Double score = 1.5;
        return score;
    }
}