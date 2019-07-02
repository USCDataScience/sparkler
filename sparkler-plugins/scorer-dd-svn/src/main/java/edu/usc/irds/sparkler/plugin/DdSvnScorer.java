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
import edu.usc.irds.sparkler.plugin.ddsvn.ApacheHttpRestClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements the @{@link Scorer} interface providing a method to determine the score of the extracted text
 * on the basis of the result obtained by using a RESTful SVN-based classifier.
 */
public class DdSvnScorer extends AbstractExtensionPoint implements Scorer {

    private final static Logger LOG = LoggerFactory.getLogger(DdSvnScorer.class);

    private Map<String, String> classes;

    private ApacheHttpRestClient restClient;

    private LinkedHashMap<String, Object> pluginConfig;

    private String uriClassifier;

    private String fallbackScore;

    private String scoreKey;

    @ConfigKey
    public static final String SCORER_DD_SVN_URL = "scorer.dd.svn.url";
    public static final String DEFAULT_SCORER_DD_SVN_URL = "http://localhost:5000/classify/predict";

    @ConfigKey
    public static final String FALLBACK_SCORE = "scorer.dd.svn.fallback";
    public static final String DEFAULT_FALLBACK_SCORE = "0";

    @ConfigKey
    public static final String SCORE_KEY = "scorer.dd.svn.key";
    public static final String DEFAULT_SCORE_KEY = "svn_score";

    @Override
    public void init(JobContext context) throws SparklerException {
        super.init(context);
        SparklerConfiguration config = jobContext.getConfiguration();
        this.pluginConfig = config.getPluginConfiguration(pluginId);
        this.uriClassifier = pluginConfig.getOrDefault(SCORER_DD_SVN_URL, DEFAULT_SCORER_DD_SVN_URL).toString();
        this.fallbackScore = pluginConfig.getOrDefault(FALLBACK_SCORE, DEFAULT_FALLBACK_SCORE).toString();
        this.scoreKey = pluginConfig.getOrDefault(SCORE_KEY, DEFAULT_SCORE_KEY).toString();

        LOG.info(SCORER_DD_SVN_URL + ": " + this.uriClassifier);
        LOG.info(FALLBACK_SCORE + ": " + this.fallbackScore);
        LOG.info(SCORE_KEY + ": " + this.scoreKey);
    }

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        this.pluginId = pluginId;
        init(context);
    }

    public DdSvnScorer() {
        this.classes = new HashMap<String,String>();
        this.classes.put("Model doesn't exist", "-1");
        this.classes.put("Not Relevant", "0");
        this.classes.put("Relevant", "1");
        this.classes.put("Highly Relevant", "2");

        this.restClient = new ApacheHttpRestClient();
    }

    @Override
    public String getScoreKey() {
        return this.scoreKey;
    }

    @Override
    public Double score(String extractedText) throws Exception {
        LOG.info(this.uriClassifier);
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("model", jobContext.getId());
        item.put("content", extractedText);
        array.add(item);
        json.put("score", array);
        LOG.info(json.toJSONString());
        String response = this.restClient.httpPostRequest(this.uriClassifier, json.toJSONString())
            .replace("\"", "").replace("\n", "");

        String scoreString = this.classes.get(response);
        LOG.info("Score string returned " + scoreString);
        Double score = Double.parseDouble(scoreString == null ? this.fallbackScore : scoreString);

        return score;
    }
}
