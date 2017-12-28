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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import edu.usc.irds.sparkler.util.FetcherDefault;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class has implements {@link edu.usc.irds.sparkler.Fetcher} using
 * <a href="http://htmlunit.sourceforge.net/"> HTMLUnit</a>.
 *
 */
@Extension
public class HtmlUnitFetcher extends FetcherDefault  implements AutoCloseable {

    private static final Integer DEFAULT_TIMEOUT = 2000;
    private static final Integer DEFAULT_JS_TIMEOUT = 2000;
    private static final Logger LOG = LoggerFactory.getLogger(HtmlUnitFetcher.class);
    private WebClient driver;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);
        //TODO: get timeouts from configurations
        driver = new WebClient(BrowserVersion.BEST_SUPPORTED);
        driver.setJavaScriptTimeout(DEFAULT_JS_TIMEOUT);
        WebClientOptions options = driver.getOptions();
        options.setCssEnabled(false);
        options.setAppletEnabled(false);
        options.setDownloadImages(false);
        options.setJavaScriptEnabled(true);
        options.setTimeout(DEFAULT_TIMEOUT);
        options.setUseInsecureSSL(true);
        options.setPopupBlockerEnabled(true);
        options.setDoNotTrackEnabled(true);
        options.setGeolocationEnabled(false);
        options.setHistorySizeLimit(2);
        options.setPrintContentOnFailingStatusCode(false);
        options.setThrowExceptionOnScriptError(false);
        options.setThrowExceptionOnFailingStatusCode(false);
        if (this.httpHeaders != null && !this.httpHeaders.isEmpty()) {
            LOG.info("Found {} headers", this.httpHeaders.size());
            this.httpHeaders.forEach((name, val) -> driver.addRequestHeader(name, val));
        } else {
            LOG.info("No user headers found");
        }
    }

    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        LOG.info("HtmlUnit FETCHER {}", resource.getUrl());
        FetchedData fetchedData;
        long startTime = System.currentTimeMillis();
        try {
            String userAgent = getUserAgent();
            if (StringUtils.isNotBlank(userAgent)) {
                driver.removeRequestHeader(USER_AGENT);
                driver.addRequestHeader(USER_AGENT, userAgent);
            }
            Page page = driver.getPage(resource.getUrl());

            WebResponse response = page.getWebResponse();
            boolean truncated = false;
            try (InputStream stream = response.getContentAsStream()) {
                try (BoundedInputStream boundedStream = new BoundedInputStream(stream, CONTENT_LIMIT)) {
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        IOUtils.copy(boundedStream, out);
                        fetchedData = new FetchedData(out.toByteArray(), response.getContentType(), response.getStatusCode(), System.currentTimeMillis() - startTime);
                        long contentLength = page.getWebResponse().getContentLength();
                        if (contentLength > 0 && contentLength < Integer.MAX_VALUE) {
                            fetchedData.setContentLength((int) contentLength);
                            truncated = (contentLength > fetchedData.getContentLength());
                            if (truncated) {
                                LOG.info("Content Truncated: {}, TotalSize={}", resource.getUrl(), contentLength);
                            }
                        }
                    }
                }
            }
            resource.setStatus(ResourceStatus.FETCHED.toString());

            List<NameValuePair> respHeaders = page.getWebResponse().getResponseHeaders();
            Map<String, List<String>> headers = new HashMap<>();
            fetchedData.setHeaders(headers);
            if (respHeaders != null && !respHeaders.isEmpty()){
                respHeaders.forEach(item -> {
                    if (!headers.containsKey(item.getName())) {
                        headers.put(item.getName(), new ArrayList<>());
                    }
                    headers.get(item.getName()).add(item.getValue());
                });
            }
            if (truncated){ //add truncated header
                headers.put(TRUNCATED, Collections.singletonList(Boolean.TRUE.toString()));
            }
        } catch (Exception e){
            LOG.warn(e.getMessage(), e);
            fetchedData = new FetchedData(new byte[0], "unknown/unknown", 0, 0); // fixme: use proper status code
            resource.setStatus(ResourceStatus.ERROR.toString());
        }
        fetchedData.setResource(resource);
        return fetchedData;
    }

    public void close() {
        if (driver != null) {
            LOG.info("Closing the JS browser");
            driver.close();
            driver = null;
        }
    }
}
