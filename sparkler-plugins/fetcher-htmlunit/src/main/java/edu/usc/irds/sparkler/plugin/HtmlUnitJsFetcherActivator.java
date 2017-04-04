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


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.irds.sparkler.Fetcher;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Loads web page on a browser and returns HTML once page is loaded.
 * Properties can be configured
 */
public class HtmlUnitJsFetcherActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(HtmlUnitJsFetcherActivator.class);
    private HtmlUnitFetcher fetcher;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOG.info("Activating HtmlUnitFetcher Plugin");
        Dictionary<String, String> prop = new Hashtable<>();
        prop.put("Fetcher", "HtmlUnitFetcher");
        fetcher = new HtmlUnitFetcher();
        bundleContext.registerService(Fetcher.class.getName(), fetcher, prop);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        LOG.info("Stopping HtmlUnitFetcher Plugin");
        if (fetcher != null) {
            fetcher.close();
        }
    }
}
