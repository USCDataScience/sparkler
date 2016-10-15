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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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

	private static final Integer DEFAULT_TIMEOUT = 2000;
	private static final Integer ERROR_CODE = 400;
	private static final Logger LOG = LoggerFactory.getLogger(FetcherJBrowser.class);
	private LinkedHashMap<String, Object> pluginConfig;
	
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
		/*
		* In this plugin we will work on only HTML data
		* If data is of any other data type like image, pdf etc plugin will return client error
		* so it can be fetched using default Fetcher
		*/
		if(! isWebPage(webUrl)){
			LOG.debug("{} not a html. Falling back to default fetcher.", webUrl);
			//This should be true for all URLS ending with 4 character file extension 
			return new FetchedData("".getBytes(), "application/html", ERROR_CODE) ;
		}
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
		
		return new FetchedData(html.getBytes(), "application/html",status) ;
	}

	private boolean isWebPage(String webUrl) {
		String contentType = "";
		try {
			URLConnection conn= new URL(webUrl).openConnection();
			contentType = conn.getHeaderField("Content-Type");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return contentType.contains("text") || contentType.contains("ml");
	}

	public JBrowserDriver createBrowserInstance() {
		Integer socketTimeout = (Integer) pluginConfig.getOrDefault("socket.timeout", DEFAULT_TIMEOUT);
		Integer connectTimeout = (Integer) pluginConfig.getOrDefault("connect.timeout", DEFAULT_TIMEOUT);

		return new JBrowserDriver(Settings.builder()
				.timezone(Timezone.AMERICA_NEWYORK)
				.quickRender(true)
				.headless(true)
				.ignoreDialogs(true)
				.ajaxResourceTimeout(DEFAULT_TIMEOUT)
				.ajaxWait(DEFAULT_TIMEOUT).socketTimeout(socketTimeout)
				.connectTimeout(connectTimeout).build());
	}

	public void quitBrowserInstance(JBrowserDriver driver) {
		driver.quit();
	}

}
