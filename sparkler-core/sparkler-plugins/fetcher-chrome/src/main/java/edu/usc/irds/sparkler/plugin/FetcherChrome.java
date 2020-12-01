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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jetty.util.ArrayUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Extension
public class FetcherChrome extends FetcherDefault {

    private static final Integer DEFAULT_TIMEOUT = 2000;
    private static final Logger LOG = LoggerFactory.getLogger(FetcherChrome.class);
    private LinkedHashMap<String, Object> pluginConfig;
    private WebDriver driver;
    private WebElement clickedEl = null;


    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        SparklerConfiguration config = jobContext.getConfiguration();
        //TODO should change everywhere 
        pluginConfig = config.getPluginConfiguration(pluginId);



        String loc = (String) pluginConfig.getOrDefault("chrome.dns", "");
        if(loc.equals("")){
            driver = new ChromeDriver();
        }
        else{
            try {
                final DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
                final ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--headless");
                desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

                driver = new RemoteWebDriver(new URL(loc), desiredCapabilities);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        LOG.info("Chrome FETCHER {}", resource.getUrl());
        FetchedData fetchedData;
        /*
		* In this plugin we will work on only HTML data
		* If data is of any other data type like image, pdf etc plugin will return client error
		* so it can be fetched using default Fetcher
		*/
        if (!isWebPage(resource.getUrl())) {
            LOG.debug("{} not a html. Falling back to default fetcher.", resource.getUrl());
            //This should be true for all URLS ending with 4 character file extension
            //return new FetchedData("".getBytes(), "application/html", ERROR_CODE) ;
            return super.fetch(resource);
        }
        long start = System.currentTimeMillis();

        LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));

        // This will block for the page load and any
        // associated AJAX requests
        driver.get(resource.getUrl());

        URL url = new URL(resource.getUrl());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(resource.getHttpMethod());
        connection.connect();

        int status = connection.getResponseCode();
        //content-type

        // Returns the page source in its current state, including
        // any DOM updates that occurred after page load

        //quitBrowserInstance(driver);

        int waittimeout = (int) pluginConfig.getOrDefault("chrome.wait.timeout", "-1");
        String waittype = (String) pluginConfig.getOrDefault("chrome.wait.type", "");
        String waitelement = (String) pluginConfig.getOrDefault("chrome.wait.element", "");

        if(waittimeout > -1){
            LOG.debug("Waiting {} seconds for element {} of type {} to become visible", waittimeout, waitelement, waittype);
            WebDriverWait wait = new WebDriverWait(driver, waittimeout);
            if(waittype.equals("class")){
                LOG.debug("waiting for class...");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(waitelement)));
            } else if(waittype.equals("name")){
                LOG.debug("waiting for name...");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(waitelement)));
            } else if(waittype.equals("id")){
                LOG.debug("waiting for id...");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(waitelement)));
            }
        }

        if (pluginConfig.get("chrome.selenium.enabled").equals("true")){
            runScript(pluginConfig.get("chrome.selenium.script"));
        }
        
        String html = driver.getPageSource();

        LOG.debug("Time taken to load {} - {} ", resource.getUrl(), (System.currentTimeMillis() - start));

        if (!(status >= 200 && status < 300)) {
            // If not fetched through plugin successfully
            // Falling back to default fetcher
            LOG.info("{} Failed to fetch the page. Falling back to default fetcher.", resource.getUrl());
            return super.fetch(resource);
        }
        fetchedData = new FetchedData(html.getBytes(), "application/html", status);
        resource.setStatus(ResourceStatus.FETCHED.toString());
        fetchedData.setResource(resource);
        return fetchedData;
    }

    private void runScript(Object orDefault) {
        if(orDefault != null && orDefault instanceof LinkedHashMap){
            LinkedHashMap mp = (LinkedHashMap) orDefault;

            Iterator it = mp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());


                LinkedHashMap submap = (LinkedHashMap) pair.getValue();
                runSubScript(submap);
                it.remove(); 
            }
        }
        LOG.debug("");
    }

    private void runSubScript(LinkedHashMap mp){
        Iterator it = mp.entrySet().iterator();
        String type = null;
        String value = null;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            if(pair.getKey().equals("input")){
                type = (String) pair.getValue();
            } else if(pair.getKey().equals("value")){
                value = (String) pair.getValue();
            } 

            it.remove(); 
        }

        if(type.equals("click")){
            clickElement(value);
        } else if (type.equals("keys")){
            typeCharacters(value);
        }
    }

    private void clickElement(String el){
        String splits[] = el.split(":");
        String type = splits[0];
        Object pruned[] = ArrayUtils.remove(splits, 0);
        String element = "";
        for (Object obj : pruned){
            element = element + obj + " ";
        }
        element = element.substring(0, element.length() - 1);
        
        if(type.equals("id")){
            clickedEl = driver.findElement(By.id(element));
        } else if(type.equals("class")){
            clickedEl = driver.findElement(By.className(element));
        } else if(type.equals("name")){
            clickedEl = driver.findElement(By.name(element));
        } else if(type.equals("xpath")){
            clickedEl = driver.findElement(By.xpath(element));
        }
        clickedEl.click();
    }

    private void typeCharacters(String chars){
        clickedEl.sendKeys(chars);
    }

    public void closeResources() {
        quitBrowserInstance(driver);
    }

    private boolean isWebPage(String webUrl) {
        try {
            URLConnection conn = new URL(webUrl).openConnection();
            String contentType = conn.getHeaderField("Content-Type");
            return contentType.contains("text") || contentType.contains("ml");
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }

    private boolean hasDriverQuit() {
        try {
            String result = driver.toString();
            return result.contains("(null)");
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }

    public void quitBrowserInstance(WebDriver driver) {
        if (driver != null) {
            if (!hasDriverQuit()) {
                try {
                    // FIXME - Exception when closing the driver. Adding an unused GET request
                    driver.get("http://www.apache.org/");
                    driver.quit();
                } catch (Exception e) {
                    LOG.debug("Exception {} raised. The driver is either already closed " +
                            "or this is an unknown exception", e.getMessage());
                }
            } else {
                LOG.debug("Driver is already quit");
            }
        } else {
            LOG.debug("Driver was null");
        }
    }

}
