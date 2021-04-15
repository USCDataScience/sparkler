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

import com.kytheralabs.SeleniumScripter;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import edu.usc.irds.sparkler.util.FetcherDefault;

import org.apache.commons.lang.ArrayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;

import org.openqa.selenium.Proxy;

@Extension
public class FetcherChrome extends FetcherDefault {

    private static final Logger LOG = LoggerFactory.getLogger(FetcherChrome.class);
    private Map<String, Object> pluginConfig;
    private WebDriver driver;
    private int latestStatus;
    private Proxy seleniumProxy;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        SparklerConfiguration config = jobContext.getConfiguration();
        // TODO should change everywhere
        pluginConfig = config.getPluginConfiguration(pluginId);

        try {
            System.out.println("Initializing Chrome Driver");
            startDriver(true);
        } catch (UnknownHostException | MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Failed to init Chrome Session");
        }
    }

    private void checkSession() {
        for(int retryloop = 0; retryloop < 10; retryloop++){
            try{
                driver.getCurrentUrl();
            } catch (Exception e) {
                System.out.println("Failed session, restarting");
                try {
                    startDriver(false);
                } catch (UnknownHostException | MalformedURLException unknownHostException) {
                    unknownHostException.printStackTrace();
                }
            }
        }
    }

    private void startDriver(Boolean restartproxy) throws UnknownHostException, MalformedURLException {
        String loc = (String) pluginConfig.getOrDefault("chrome.dns", "");
        if (loc.equals("")) {
            driver = new ChromeDriver();
        } else {
            BrowserUpProxy proxy = new BrowserUpProxyServer();
            proxy.setTrustAllServers(true);

            proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
            proxy.addResponseFilter(new ResponseFilter() {
                @Override
                public void filterResponse(HttpResponse response, HttpMessageContents contents,
                                           HttpMessageInfo messageInfo) {
                    latestStatus = response.getStatus().code();
                }
            });

            String paddress = (String) pluginConfig.getOrDefault("chrome.proxy.address", "auto");

            if (paddress.equals("auto")) {
                proxy.start();
                int port = proxy.getPort();
                seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            } else {
                if(restartproxy) {
                    String[] s = paddress.split(":");
                    proxy.start(Integer.parseInt(s[1]));
                    InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(s[0]), Integer.parseInt(s[1]));
                    seleniumProxy = ClientUtil.createSeleniumProxy(addr);
                }
            }

            // seleniumProxy.setHttpProxy("172.17.146.238:"+Integer.toString(port));
            // seleniumProxy.setSslProxy("172.17.146.238:"+Integer.toString(port));

            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            final ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("--ignore-certificate-errors");
            chromeOptions.addArguments("--incognito");
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

            if(loc.equals("local")){
                driver = new ChromeDriver(capabilities);
            } else{
                driver = new RemoteWebDriver(new URL(loc), capabilities);
            }
        }

    }
    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        startDriver(false);
        LOG.info("Chrome FETCHER {}", resource.getUrl());
        FetchedData fetchedData;
        JSONObject json = null;
        /*
         * In this plugin we will work on only HTML data If data is of any other data
         * type like image, pdf etc plugin will return client error so it can be fetched
         * using default Fetcher
         */
        if (!isWebPage(resource.getUrl())) {
            LOG.debug("{} not a html. Falling back to default fetcher.", resource.getUrl());
            // This should be true for all URLS ending with 4 character file extension
            // return new FetchedData("".getBytes(), "application/html", ERROR_CODE) ;
            return super.fetch(resource);
        }
        long start = System.currentTimeMillis();

        LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));

        if(resource.getMetadata()!=null && !resource.getMetadata().equals("")){
            json = processMetadata(resource.getMetadata());

        }
        // This will block for the page load and any
        // associated AJAX requests

        try {
            checkSession();
        } catch (Exception e){
            System.out.println("failed to start selenium session");
        }
        driver.get(resource.getUrl());

        int waittimeout = (int) pluginConfig.getOrDefault("chrome.wait.timeout", "-1");
        String waittype = (String) pluginConfig.getOrDefault("chrome.wait.type", "");
        String waitelement = (String) pluginConfig.getOrDefault("chrome.wait.element", "");

        if (waittimeout > -1) {
            LOG.debug("Waiting {} seconds for element {} of type {} to become visible", waittimeout, waitelement,
                    waittype);
            WebDriverWait wait = new WebDriverWait(driver, waittimeout);
            switch (waittype) {
                case "class":
                    LOG.debug("waiting for class...");
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(waitelement)));
                    break;
                case "name":
                    LOG.debug("waiting for name...");
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(waitelement)));
                    break;
                case "id":
                    LOG.debug("waiting for id...");
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(waitelement)));
                    break;
            }
        }
        SeleniumScripter scripter = new SeleniumScripter(driver);
        String seleniumenabled = (String) pluginConfig.getOrDefault("chrome.selenium.enabled", "false");
        String html = null;
        if (seleniumenabled.equals("true")) {
            if(pluginConfig.get("chrome.selenium.script") != null && pluginConfig.get("chrome.selenium.script") instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) pluginConfig.get("chrome.selenium.script");
                try {
                    scripter.runScript(map, null, null);
                } catch (Exception ignored){

                }
                List<String> snapshots = scripter.getSnapshots();
                html = String.join(",", snapshots);
            }
        }
        if(json != null && json.containsKey("selenium")){
            if(json.get("selenium") != null && json.get("selenium") instanceof Map) {
                try {
                    scripter.runScript((Map<String, Object>) json.get("selenium"), null, null);
                } catch (Exception e){
                    Map<String, Object> tempmap = new HashMap<>();
                    tempmap.put("type", "file");
                    tempmap.put("targetdir", "/dbfs/FileStore/screenshots/"+resource.getCrawlId()+System.currentTimeMillis());
                    scripter.screenshot(tempmap);
                    e.printStackTrace();
                }
                List<String> snapshots = scripter.getSnapshots();
                html = String.join(",", snapshots);
            }
        }

        if(html == null) {
            html = driver.getPageSource();
        }

        LOG.debug("Time taken to load {} - {} ", resource.getUrl(), (System.currentTimeMillis() - start));

        System.out.println("LATEST STATUS: "+latestStatus);
        /*if (!(latestStatus >= 200 && latestStatus < 300) && latestStatus != 0) {
            // If not fetched through plugin successfully
            // Falling back to default fetcher
            LOG.info("{} Failed to fetch the page. Falling back to default fetcher.", resource.getUrl());
            return super.fetch(resource);
        }*/

        fetchedData = new FetchedData(html.getBytes(), "text/html", latestStatus);
        resource.setStatus(ResourceStatus.FETCHED.toString());
        fetchedData.setResource(resource);
        driver.quit();
        driver = null;
        return fetchedData;
    }

    private JSONObject processMetadata(String metadata) {
        if(metadata != null){
            JSONParser parser = new JSONParser();
            JSONObject json;
            try {
                json = (JSONObject) parser.parse(metadata);
                return json;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    private boolean isWebPage(String webUrl) {
        try {
            URL url = new URL(webUrl);
            CookieManager cm = new java.net.CookieManager();
            CookieHandler.setDefault(cm);
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            String contentType = conn.getHeaderField("Content-Type");
            return contentType.contains("text") || contentType.contains("ml") || conn.getResponseCode() == 302;
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }

}
