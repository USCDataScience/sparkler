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

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Fetcher;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;

public class FetcherJBrowser extends AbstractExtensionPoint implements Fetcher {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final Logger LOG = LoggerFactory.getLogger(FetcherJBrowser.class);
	private LinkedHashMap<String, String> pluginConfig;
	
	@Override
    public void init(JobContext context) throws SparklerException {
        super.init(context);
        
        SparklerConfiguration config = jobContext.getConfiguration();
        //TODO should change everywhere 
        pluginConfig = config.getPluginConfiguration(pluginId);
    }

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        this.pluginId = pluginId;
        init(context);
    }
    
	@Override
	public FetchedData fetch(String webUrl) {
		long start = System.currentTimeMillis();

		JBrowserDriver driver = createBrowserInstance();

		LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));

		// This will block for the page load and any
		// associated AJAX requests
		driver.get(webUrl);

		int status = driver.getStatusCode();
		//content-type
		
		// Returns the page source in its current state, including
		// any DOM updates that occurred after page load
		String html = driver.getPageSource();
		quitBrowserInstance(driver);
		
		LOG.debug("Time taken to load {} - {} ",webUrl, (System.currentTimeMillis() - start));
		
		return null ;
	}

	public JBrowserDriver createBrowserInstance() {
		// TODO: Take from property file
		int socketTimeout = Integer.parseInt(pluginConfig.get("socket.timeout"));
		int connectTimeout = Integer.parseInt(pluginConfig.get("connect.timeout"));
		
		return new JBrowserDriver(Settings.builder()
				.timezone(Timezone.AMERICA_NEWYORK)
				.ajaxResourceTimeout(DEFAULT_TIMEOUT)
				.ajaxWait(DEFAULT_TIMEOUT).socketTimeout(socketTimeout)
				.connectTimeout(connectTimeout).build());
	}

	public void quitBrowserInstance(JBrowserDriver driver) {
		driver.quit();
	}

}
