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

import edu.usc.irds.sparkler.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RegexURLFilterTest {

    @Test
    public void testFilter() throws Exception {
        //TODO: Get pluginId from OSGI Context
        RegexURLFilter filter = TestUtils.newInstance(RegexURLFilter.class, "urlfilter.regex");
        Map<String, Boolean> expectations = new HashMap<String, Boolean>(){{
            put("http://apache.org", true);
            put("http://irds.usc.edu", true);
            put("https://twitter.com/thammegowda", true);
            put("https://twitter.com/profile.png", false);
            put("https://twitter.com/profile.PNG", false);
            put("mailto:", false);
            put("file:///home/tg/", false);

            //JPG s are overriden in src/test/ file to test the resource override for test cases
            put("https://twitter.com/profile.jpg", true);
            put("https://twitter.com/profile.JPG", true);
        }};
        expectations.forEach((url, expected) -> Assert.assertEquals(expected, filter.filter(url, url)));
    }
}