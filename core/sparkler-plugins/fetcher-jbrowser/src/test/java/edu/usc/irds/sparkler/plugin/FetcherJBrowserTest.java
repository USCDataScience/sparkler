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

import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.util.TestUtils;

//TODO - place html and js file in a test folder and serve it through a embeded server in junit
public class FetcherJBrowserTest {

	/**
	 * Test HTML pages should be served through Jbrowser
	 */
//	TODO - Uncomment after using test server
//    @Test
    public void testJBrowserHtml() throws Exception {
    	FetcherJBrowser fetcherJBrowser = TestUtils.newInstance(FetcherJBrowser.class, "fetcher.jbrowser");
		Resource resource = new Resource("http://nutch.apache.org", "nutch.apache.org", TestUtils.JOB_CONTEXT);
    	System.out.println(fetcherJBrowser.fetch(resource).getResponseCode());
    }
    
    /**
	 * Test Images should be skipped by Jbrowser
	 */
//	TODO - Uncomment after using test server
//    @Test
    public void testJBrowserImage() throws Exception {
    	FetcherJBrowser fetcherJBrowser = TestUtils.newInstance(FetcherJBrowser.class, "fetcher.jbrowser");
		Resource resource = new Resource("http://nutch.apache.org/assets/img/nutch_logo_tm.png", "nutch.apache.org", TestUtils.JOB_CONTEXT);
    	System.out.println(fetcherJBrowser.fetch(resource).getResponseCode());
    }
}