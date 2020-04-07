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


import edu.usc.irds.sparkler.ConfigKey;
import edu.usc.irds.sparkler.plugin.regex.RegexRule;
import edu.usc.irds.sparkler.plugin.regex.RegexURLFilterBase;
import org.pf4j.Extension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;


/**
 * Filters URLs based on a file of regular expressions using the
 * {@link java.util.regex Java Regex implementation}.
 */
@Extension
public class RegexURLFilter extends RegexURLFilterBase {

    @ConfigKey
    public static final String URLFILTER_REGEX_FILE = "urlfilter.regex.file";

    /**
     * Rules specified as a config property will override rules specified as a
     * config file.
     */
    protected Reader getRulesReader(LinkedHashMap pluginConfig) throws IOException {
        String regexFile = pluginConfig.get(URLFILTER_REGEX_FILE).toString();
        return new InputStreamReader(jobContext.getClass().getClassLoader().getResource(regexFile).openStream());
    }

    // Inherited Javadoc
    protected Rule createRule(boolean sign, String regex) {
        return new Rule(sign, regex);
    }

    protected Rule createRule(boolean sign, String regex, String hostOrDomain) {
        return new Rule(sign, regex, hostOrDomain);
    }

    private class Rule extends RegexRule {

        private Pattern pattern;

        Rule(boolean sign, String regex) {
            this(sign, regex, null);
        }

        Rule(boolean sign, String regex, String hostOrDomain) {
            super(sign, regex, hostOrDomain);
            pattern = Pattern.compile(regex);
        }

        protected boolean match(String url) {
            return pattern.matcher(url).find();
        }
    }

}