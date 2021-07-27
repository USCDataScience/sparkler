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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@Extension
public class FetcherChrome extends FetcherDefault {

    private static final Logger LOG = LoggerFactory.getLogger(FetcherChrome.class);
    private Map<String, Object> pluginConfig;
    private WebDriver driver;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        SparklerConfiguration config = jobContext.getConfiguration();
        // TODO should change everywhere
        pluginConfig = config.getPluginConfiguration(pluginId);

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
    /**
     * Gets a user agent from a list of configured values, rotates the list for each call
     * @return get a user agent string from the list of configured values
     */
    public String getUserAgent(){
        if (userAgents == null || userAgents.isEmpty()){
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        }
        String agent = userAgents.get(userAgentIndex);
        synchronized (this) { // rotate index
            userAgentIndex = (userAgentIndex + 1) % userAgents.size();
        }
        return agent;
    }

    private void startDriver(Boolean restartproxy) throws UnknownHostException, MalformedURLException {
        String loc = (String) pluginConfig.getOrDefault("chrome.dns", "");
        if (loc.equals("")) {
            driver = new ChromeDriver();
        } else {
            final ChromeOptions chromeOptions = new ChromeOptions();

            List<String> chromedefaults = Arrays.asList("--auto-open-devtools-for-tabs", "--headless", "--no-sandbox", "--disable-gpu", "--disable-extensions",
                    "--ignore-certificate-errors",  "--incognito", "--window-size=1920,1080", "--proxy-server='direct://",
                    "--proxy-bypass-list=*", "--disable-background-networking", "--safebrowsing-disable-auto-update",
                    "--disable-sync", "--metrics-recording-only", "--disable-default-apps", "--no-first-run",
                    "--disable-setuid-sandbox", "--hide-scrollbars", "--no-zygote", "--disable-notifications",
                    "--disable-logging", "--disable-permissions-api");

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            logPrefs.enable(LogType.PROFILER, Level.ALL);
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            chromeOptions.setCapability("goog:loggingPrefs", logPrefs);
            List<String> vals = (List<String>) (pluginConfig.getOrDefault("chrome.options", chromedefaults));
            vals.add("--user-agent="+getUserAgent());
            chromeOptions.addArguments(vals);

            chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

            if(loc.equals("local")){
                driver = new ChromeDriver(chromeOptions);
                driver.manage().timeouts().pageLoadTimeout(3600, TimeUnit.SECONDS);
            } else{
                driver = new RemoteWebDriver(new URL(loc), chromeOptions);
            }

            driver.manage().window().setSize(new Dimension(1920, 1080));
            driver.manage().window().maximize();
            LOG.info("The Chrome Window size is: "+driver.manage().window().getSize());
        }

    }
    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        startDriver(false);
        LOG.info("Chrome FETCHER {}", resource.getUrl());
        try {
            checkSession();
        } catch (Exception e){
            System.out.println("failed to start selenium session");
        }
        long start = System.currentTimeMillis();
        FetchedData fetchedData;

        /*
         * In this plugin we will work on only HTML data If data is of any other data
         * type like image, pdf etc plugin will return client error so it can be fetched
         * using default Fetcher
         */
        if (!isWebPage(resource.getUrl())) {
            LOG.info("{} not a html. Falling back to default fetcher.", resource.getUrl());
            // This should be true for all URLS ending with 4 character file extension
            // return new FetchedData("".getBytes(), "application/html", ERROR_CODE) ;
            return super.fetch(resource);
        } else{
            fetchedData = htmlFlow(resource, start);
        }



        LOG.debug("Time taken to load {} - {} ", resource.getUrl(), (System.currentTimeMillis() - start));


        driver.quit();
        driver = null;

        return fetchedData;
    }

    public FetchedData dataFlow(Resource resource, long start){

        return null;
    }

    public FetchedData htmlFlow(Resource resource, long start) throws IOException, java.text.ParseException {
        FetchedData fetchedData;


        LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));
        JSONObject json = null;

        if(resource.getMetadata()!=null && !resource.getMetadata().equals("")){
            json = processMetadata(resource.getMetadata());

        }
        // This will block for the page load and any
        // associated AJAX requests


        driver.get(resource.getUrl());
        String waitforready = pluginConfig.getOrDefault("chrome.selenium.javascriptready", "false").toString();

        if(waitforready.equals("true")){
            new WebDriverWait(driver, 60)
                    .until((driver) -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                            .toString()
                            .equals("complete"));
        }

        String html = null;
        String globalscript = pluginConfig.getOrDefault("chrome.selenium.script", "").toString();

        if(!globalscript.equals("")){
            SeleniumScripter scripter = new SeleniumScripter(driver);

            Map m = (Map<String, Object>) json.get("chrome.selenium.script");
            Map jsonmap = new TreeMap(m);

            executeSeleniumScript(jsonmap, scripter, resource);

            List<String> snapshots = scripter.getSnapshots();
            html = String.join(",", snapshots);
        } else if(json != null && json.containsKey("selenium")){
            if(json.get("selenium") != null && json.get("selenium") instanceof Map) {
                SeleniumScripter scripter = new SeleniumScripter(driver);

                Map m = (Map<String, Object>) json.get("selenium");
                Map jsonmap = new TreeMap(m);

                executeSeleniumScript(jsonmap, scripter, resource);

                List<String> snapshots = scripter.getSnapshots();
                html = String.join(",", snapshots);
            }
        }

        if(html == null) {
            String screenshotcapture = pluginConfig.getOrDefault("chrome.selenium.screenshotoncapture", "false").toString();
            if(screenshotcapture.equals("true")){
                SeleniumScripter htmlscripter = new SeleniumScripter(driver);

                Map<String, Object> tempmap = new HashMap<>();
                tempmap.put("type", "file");
                Path path = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(), jobContext.getId(), "screencaptures");
                File f = path.toFile();
                f.mkdirs();
                Path filepath = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(), jobContext.getId(), "screencaptures");
                tempmap.put("targetdir", filepath.toString());
                tempmap.put("tag", resource.getId());
                htmlscripter.setOutputPath("");
                htmlscripter.screenshotOperation(tempmap);
            }
            html = driver.getPageSource();
        }

        if(!resource.getStatus().equals(ResourceStatus.ERROR.toString())){
            resource.setStatus(ResourceStatus.FETCHED.toString());
        }
        fetchedData = new FetchedData(html.getBytes(), "text/html", 200);
        fetchedData.setResource(resource);

        return fetchedData;
    }

    private void executeSeleniumScript(Map jsonmap, SeleniumScripter scripter, Resource resource) throws IOException, java.text.ParseException {
        try {
            LOG.info("Running Selenium Script");
            Path filepath = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(), jobContext.getId());
            scripter.setOutputPath(filepath.toString());
            scripter.runScript(jsonmap);

            resource.setStatus(ResourceStatus.FETCHED.toString());
        } catch (Exception e){
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            List<LogEntry> alllogs = logs.getAll();
            for(LogEntry logentry: alllogs){
                LOG.info(logentry.getMessage());
            }
            logs = driver.manage().logs().get(LogType.PERFORMANCE);
            alllogs = logs.getAll();
            for(LogEntry logentry: alllogs){
                LOG.info(logentry.getMessage());
            }
            logs = driver.manage().logs().get(LogType.PROFILER);
            alllogs = logs.getAll();
            for(LogEntry logentry: alllogs){
                LOG.info(logentry.getMessage());
            }
            if(pluginConfig.containsKey("chrome.selenium.outputdirectory")) {
                Map<String, Object> tempmap = new HashMap<>();
                tempmap.put("type", "file");
                Path path = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(), jobContext.getId(), "errors");
                File f = path.toFile();
                f.mkdirs();
                tempmap.put("targetdir", "errors");
                scripter.screenshotOperation(tempmap);
            }
            LOG.error("Caught an exception in  Selenium Scripter: " + e);

            resource.setStatus(ResourceStatus.ERROR.toString());
        }

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
            if(contentType == null && conn.getResponseCode() == 302){
                return isWebPage(conn.getHeaderField("Location"));
            }
            return contentType.contains("json") || contentType.contains("text") || contentType.contains("ml");
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }

}
