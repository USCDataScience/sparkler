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

import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlUnitFetcherTest {
	@Test
	public void testJBrowserImage() throws Exception {
		HtmlUnitFetcher htmlUnitFetcher = TestUtils.newInstance(HtmlUnitFetcher.class, "fetcher.htmlunit");
		Resource resource = new Resource("http://nutch.apache.org/assets/img/nutch_logo_tm.png", "nutch.apache.org", TestUtils.JOB_CONTEXT);
		FetchedData data = htmlUnitFetcher.fetch(resource);
		assertEquals(200, data.getResponseCode());
		assertEquals("image/png", data.getContentType());
	}

    @Test
    public void testJBrowserHtml() throws Exception {
    	HtmlUnitFetcher htmlUnitFetcher = TestUtils.newInstance(HtmlUnitFetcher.class, "fetcher.htmlunit");
		Resource resource = new Resource("http://nutch.apache.org", "nutch.apache.org", TestUtils.JOB_CONTEXT);

		FetchedData data = htmlUnitFetcher.fetch(resource);
		assertEquals(200, data.getResponseCode());
		assertEquals("text/html", data.getContentType());
	}

	@Test
	public void testJavaScript() throws Exception {
		HtmlUnitFetcher htmlUnitFetcher = TestUtils.newInstance(HtmlUnitFetcher.class, "fetcher.htmlunit");
		Resource resource = new Resource(
				"http://localhost:8080/res/jspage.html",
				"localhost", TestUtils.JOB_CONTEXT);

		FetchedData data = htmlUnitFetcher.fetch(resource);
		assertEquals(200, data.getResponseCode());
		assertEquals("text/html", data.getContentType());
		//TODO: assert that JS was executed
	}
}