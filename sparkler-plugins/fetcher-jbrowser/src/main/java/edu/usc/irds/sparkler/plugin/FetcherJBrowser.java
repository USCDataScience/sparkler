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

import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import edu.usc.irds.sparkler.util.FetcherDefault;
import org.apache.commons.io.IOUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetcherJBrowser extends FetcherDefault {
    private static final String HTML_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
    private static final Integer DEFAULT_TIMEOUT = 1000;
    private static final Integer MAX_SIZE_PAGE_KB = 500;
    private static final Logger LOG = LoggerFactory.getLogger(FetcherJBrowser.class);
    private LinkedHashMap<String, Object> pluginConfig;
    private Integer connectTimeout;

    @Override
    public void init(JobContext context) throws SparklerException {
        super.init(context);

        SparklerConfiguration config = jobContext.getConfiguration();
        //TODO should change everywhere
        pluginConfig = config.getPluginConfiguration(pluginId);
        connectTimeout = (Integer) pluginConfig.getOrDefault("connect.timeout", DEFAULT_TIMEOUT);
    }

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        this.pluginId = pluginId;
        init(context);
    }

    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        try {
            URLConnection urlConn = new URL(resource.url()).openConnection();
            HttpURLConnection connection;

            if (urlConn != null && urlConn.getDate() > 0) {
                connection = (HttpURLConnection) urlConn;
            }
            else
                throw new FetchedException();

            if (connection.getContentLengthLong() > MAX_SIZE_PAGE_KB*1000)
                throw new FetchedException();

            connection.setConnectTimeout(connectTimeout);

            int responseCode = connection.getResponseCode();
            if (responseCode > 308)
                throw new FetchedException();

            InputStream inStream = connection.getInputStream();
            byte[] rawData = IOUtils.toByteArray(inStream);
            FetchedData fetchedData = new FetchedData(rawData, connection.getContentType(), responseCode);
            resource.setStatus(ResourceStatus.FETCHED.toString());
            fetchedData.setResource(resource);

            // URL
            Pattern pTag = Pattern.compile(HTML_TAG_PATTERN);
            Pattern pLink = Pattern.compile(HTML_HREF_TAG_PATTERN);
            Matcher mTag = pTag.matcher(IOUtils.toString(rawData,
                String.valueOf(StandardCharsets.UTF_8)));

            //http://www.java2s.com/Code/Java/Regular-Expressions/Findallmatches.htm
            while (mTag.find()) {
                String href = mTag.group(1);     // get the values of href
                Matcher mLink = pLink.matcher(href);
                while (mLink.find()) {
                    String link = mLink.group(1).replaceAll("^\"|\"$|^'|'$", "");;
                    fetchedData.addOutlink(link);
                }
            }
            return fetchedData;
        }
        catch (HttpStatusException e) {
            LOG.warn("FETCH-Jsoup http status error {}", e.getStatusCode(), resource.url());
            return this.fakeFetchedData(resource, ResourceStatus.ERROR);
        }
        catch (FetchedException e) {
            LOG.warn("FETCH-Jsoup FetchedException {}", resource.url());
            return this.fakeFetchedData(resource, ResourceStatus.ERROR);
        }
        catch (SocketTimeoutException e) {
            LOG.warn("FETCH-Jsoup timeout {}", resource.url());
            return this.fakeFetchedData(resource, ResourceStatus.TIMEOUT);
        }
        catch (UnsupportedMimeTypeException e) {
            LOG.warn("FETCH-Jsoup unsupported mime type {}", resource.url());
            return this.fakeFetchedData(resource, ResourceStatus.ERROR);
        }
        catch (Exception e) {
            LOG.warn("FETCH-Jsoup EXCEPTION {}", resource.url());
            return super.fetch(resource);
        }
    }

    public void closeResources() {
    }

    private class FetchedException extends Exception {
    }
}
