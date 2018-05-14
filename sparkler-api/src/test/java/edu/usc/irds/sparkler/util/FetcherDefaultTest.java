/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
/**
 * @since 12/28/16
 */
public class FetcherDefaultTest {

    private JobContext job = TestUtils.JOB_CONTEXT;
    private FetcherDefault fetcher;
    {
        try {
            fetcher = TestUtils.newInstance(FetcherDefault.class, "");
        } catch (SparklerException e) {
            throw new RuntimeException(e);
        }
    }
    private Resource indexPage = new Resource("http://localhost:8080/res/index.html", "localhost", job);
    private Resource jsPage = new Resource("http://localhost:8080/res/jspage.html", "localhost", job);
    private Resource notFound = new Resource("http://localhost:8080/res/vacduyc_NOT_FOUND.html", "localhost", job);

    private List<Resource> resources = Arrays.asList(indexPage, jsPage, notFound);

    /**
     * Tests if a classpath is setup correctly
     */
    @Test
    public void testClasspath() {
        assertNotNull(getClass().getClassLoader().getResource("domain-suffixes.xml"));
        assertNotNull(getClass().getClassLoader().getResource(Constants.file.SPARKLER_DEFAULT));
    }

    @Test
    public void testUserAgentRotation(){
        String ua1 = fetcher.getUserAgent();
        String ua2 = fetcher.getUserAgent();
        assertNotSame(ua1, ua2);
    }

    @Test
    public void fetch() throws Exception {
        long start = System.currentTimeMillis();
        FetchedData data1 = fetcher.fetch(indexPage);
        assertEquals("text/html", data1.getContentType());
        assertNotNull(data1.getFetchedAt());
        assertTrue(data1.getFetchedAt().getTime() >= start);
        assertTrue(data1.getFetchedAt().getTime() <= System.currentTimeMillis());
        assertEquals(200, data1.getResponseCode());

        try {
            fetcher.fetch(notFound);
            fail("Exception expected");
        } catch (Exception e){
            //expected
        }
    }

    @Test
    public void fetch1() throws Exception {
        Iterator<FetchedData> stream = fetcher.fetch(resources.iterator());
        List<FetchedData> list = new ArrayList<>();
        stream.forEachRemaining(list::add);
        assertEquals(list.size(), resources.size());

        for (FetchedData data : list) {
            if (data.getResource().getUrl().contains("NOT_FOUND")) {
                assertEquals(404, data.getResponseCode());
            } else {
                assertEquals(200, data.getResponseCode());
            }
        }
    }

    @Test
    public void testReadTimeout() throws Exception {

        String url = "http://localhost:8080/slavesite?action=read-timeout&timeout=";
        url += FetcherDefault.READ_TIMEOUT + 1000;
        Resource timeoutPage = new Resource(url, "localhost", job);
        try {
            fetcher.fetch(timeoutPage);
            fail("Exception expected");
        } catch (SocketTimeoutException e){
            // pass ; expected
        }
        catch (Exception e){
            fail("Socket Exception expected, but found:" + e.getClass().getName());
        }
    }

    @Test
    public void testHeaders() throws Exception {
        /**
         * This test case tests two functionality
         * 1. A custom header can be sent to requests
         * 2. User agent is set
         */
        String url = "http://localhost:8080/slavesite?action=return-headers";
        Resource page = new Resource(url, "localhost", job);
        FetchedData fetchedData = fetcher.fetch(page);

        boolean headerFound = false;
        boolean userAgentFound = false;
        String[] lines = new String(fetchedData.getContent()).split("\n");
        for (String line : lines) {
            String[] split = line.trim().split(":");
            if (split[0].equals("Custom-Header")){
                headerFound = split[1].trim().equals("Custom Header Value");
            }
            if (split[0].equals("User-Agent")){
                userAgentFound = split[1].trim().contains("Sparkler");
            }
        }
        assertTrue(headerFound);
        assertTrue(userAgentFound);
    }
}