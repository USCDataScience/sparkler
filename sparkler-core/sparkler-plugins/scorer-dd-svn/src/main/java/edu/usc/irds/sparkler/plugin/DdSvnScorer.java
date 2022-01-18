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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import edu.usc.irds.sparkler.*;
import edu.usc.irds.sparkler.plugin.ddsvn.ApacheHttpRestClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pf4j.Extension;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the @{@link Scorer} interface providing a method to determine the score of the extracted text
 * on the basis of the result obtained by using a RESTful SVN-based classifier.
 */
@Extension
public class DdSvnScorer extends AbstractExtensionPoint implements Scorer {

    private final static Logger LOG = new LoggerContext().getLogger(DdSvnScorer.class);

    //private Map<String, String> classes;

    private ApacheHttpRestClient restClient= new ApacheHttpRestClient();

    private Map<String, Object> pluginConfig;

    private String uriClassifier;

    private String fallbackScore;

    private String scoreKey;

    private Map<String,String> classes = new HashMap() {{
        put("Model doesn't exist", "-1");
        put("Not Relevant", "0");
        put("Relevant", "1");
        put("Highly Relevant", "2");
    }};
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
    public String getScoreKey() {
        return this.scoreKey;
    }

    @Override
    public Double score(String extractedText) throws Exception {
        Map<String, Object> cfg = jobContext.getConfiguration().getPluginConfiguration(pluginId);
        this.uriClassifier = cfg.getOrDefault(SCORER_DD_SVN_URL, DEFAULT_SCORER_DD_SVN_URL).toString();
        this.fallbackScore = cfg.getOrDefault(FALLBACK_SCORE, DEFAULT_FALLBACK_SCORE).toString();
        this.scoreKey = cfg.getOrDefault(SCORE_KEY, DEFAULT_SCORE_KEY).toString();
        LOG.info(SCORER_DD_SVN_URL + ": " + this.uriClassifier);
        LOG.info(FALLBACK_SCORE + ": " + this.fallbackScore);
        LOG.info(SCORE_KEY + ": " + this.scoreKey);

        LOG.info(this.uriClassifier);
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("model", jobContext.getId());
        item.put("content", extractedText);
        array.add(item);
        json.put("score", array);
        String response = this.restClient.httpPostJSONRequest(this.uriClassifier, json.toJSONString())
            .replace("\"", "").replace("\n", "");

        String scoreString = this.classes.get(response);
        LOG.info("Score string returned " + scoreString);
        Double score = Double.parseDouble(scoreString == null ? this.fallbackScore : scoreString);

        return score;
    }
}
