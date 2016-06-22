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

// JDK imports

import edu.usc.irds.sparkler.plugin.regex.RegexRule;
import edu.usc.irds.sparkler.plugin.regex.RegexURLFilterBase;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;


/**
 * Filters URLs based on a file of regular expressions using the
 * {@link java.util.regex Java Regex implementation}.
 */
public class RegexURLFilter extends RegexURLFilterBase {

    public static final String URLFILTER_REGEX_FILE = "urlfilter.regex.file";
    public static final String URLFILTER_REGEX_RULES = "urlfilter.regex.rules";

    public RegexURLFilter() {
        super();
    }


    public RegexURLFilter(Reader reader) throws IOException, IllegalArgumentException {
        super(reader);
    }

  /*
   * ----------------------------------- * <implementation:RegexURLFilterBase> *
   * -----------------------------------
   */

    /**
     * Rules specified as a config property will override rules specified as a
     * config file.
     */
    protected Reader getRulesReader() throws IOException {
        //TODO: FIXME
        return new StringReader("+http.*");
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

    public static void main(String args[]) throws IOException {
        System.out.println("Starting...");
        RegexURLFilter filter = new RegexURLFilter();
        main(filter, args);
        System.out.println("Done...");
    }

}