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

import edu.usc.irds.sparkler.URLFilter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Activates the RegexURL Filter Plugin and register it
 * as a Service in Apache Felix
 */
public class RegexURLFilterActivator implements BundleActivator {

    // Logger for the class
    private static final Logger LOG = LoggerFactory.getLogger(RegexURLFilterActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOG.info("Activating RegexURL Plugin");
        Dictionary<String, String> prop = new Hashtable<>();
        prop.put("URLFilter", "RegexURLFilter");
        bundleContext.registerService(URLFilter.class.getName(), new RegexURLFilter(), prop);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        LOG.info("Stopping RegexURL Plugin");
    }
}
