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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import uk.co.spicule.magnesium_script.Program;
import uk.co.spicule.seleniumscripter.SeleniumScripter;
import uk.co.spicule.magnesium_script.MagnesiumScript;
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
import org.apache.commons.lang.NotImplementedException;

import java.io.*;
import java.net.MalformedURLException;

import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Extension
public class FetcherChrome extends FetcherDefault {
    private List<String> proxyEndpoints = new ArrayList<>();

    enum ScriptType {
        SELENIUM, MAGNESIUM
    }

    private static final Logger LOG = new LoggerContext().getLogger(FetcherChrome.class);
    private Map<String, Object> pluginConfig;
    private WebDriver driver;
    private ScriptType type;
    private HashMap<String, String> contentMap = new HashMap<>();

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        contentMap.put("application/json", "json");
        contentMap.put("text/html", "html");

        SparklerConfiguration config = jobContext.getConfiguration();
        // TODO should change everywhere
        pluginConfig = config.getPluginConfiguration(pluginId);
    }

    /**
     * Dumps the driver logs into the current stack trace
     */
    private void dumpLogs() {
        LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
        List<LogEntry> alllogs = logs.getAll();
        for (LogEntry logentry : alllogs) {
            LOG.info(logentry.getMessage());
        }
        logs = driver.manage().logs().get(LogType.PERFORMANCE);
        alllogs = logs.getAll();
        for (LogEntry logentry : alllogs) {
            LOG.info(logentry.getMessage());
        }
        logs = driver.manage().logs().get(LogType.PROFILER);
        alllogs = logs.getAll();
        for (LogEntry logentry : alllogs) {
            LOG.info(logentry.getMessage());
        }
    }

    /**
     * Gets a user agent from a list of configured values, rotates the list for each
     * call
     *
     * @return get a user agent string from the list of configured values
     */
    public String getUserAgent() {
        if (userAgents == null || userAgents.isEmpty()) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        }
        String agent = userAgents.get(userAgentIndex);
        synchronized (this) { // rotate index
            userAgentIndex = (userAgentIndex + 1) % userAgents.size();
        }
        return agent;
    }

    private String getProxyEndpoint(List<String> endpoints) {

        String selectedProxyEndpoint = "";
        LOG.info("Looking up endpoint");
        int totalEndpoints = endpoints.size();
        LOG.info("Endpoint quantity: " + endpoints.size());
        if(totalEndpoints > 0) {

            Random randomObj = new Random();
            LOG.info("Looking up raandom number");
            int endpointNumber = randomObj.nextInt(totalEndpoints);
            LOG.info("Random number is: " + endpointNumber);
            selectedProxyEndpoint = endpoints.get(endpointNumber);
            LOG.info("Selected endpoint: " + selectedProxyEndpoint);
            endpoints.remove(endpointNumber);
            LOG.info("Endpoint removed");
            proxyEndpoints = endpoints;
        }

        return selectedProxyEndpoint;
    }
    /**
     * Start the Selenium web driver
     *
     * @throws UnknownHostException  Occurs when the host was not resolved
     * @throws MalformedURLException Occurs when the URI given is invalid
     */
    private void startDriver() throws Exception {
        String loc = (String) pluginConfig.getOrDefault("chrome.dns", "");
        String proxyaddress = (String) pluginConfig.getOrDefault("chrome.proxy.address", "");

        LOG.info("DNS Location: " + loc);
        LOG.info("Proxy URLs: " + proxyaddress);
        if (loc.equals("")) {
            LOG.info("No DNS found, spinning up local chrome");
            driver = new ChromeDriver();
        } else {
            final ChromeOptions chromeOptions = new ChromeOptions();
            if(!proxyaddress.equals("")){
                LOG.info("Configuring proxy");
                proxyEndpoints = new LinkedList<>(Arrays.asList(proxyaddress.split(",")));
                LOG.info("Proxies Available: " + proxyEndpoints.size());
                String proxyEndpoint = getProxyEndpoint(proxyEndpoints);
                LOG.info("Selected Endpoint: " + proxyEndpoint);
                ProxySelector proxySelector = new ProxySelector(proxyEndpoint);
                Proxy p = proxySelector.getProxy();
                chromeOptions.setCapability("proxy", p);
                LOG.info("Setting up proxy: "+p.getHttpProxy());
            }


            List<String> chromedefaults = Arrays.asList("--auto-open-devtools-for-tabs", "--headless", "--no-sandbox",
                    "--disable-gpu", "--disable-extensions", "--ignore-certificate-errors", "--incognito",
                    "--window-size=1920,1080",
                    "--disable-background-networking", "--safebrowsing-disable-auto-update", "--disable-sync",
                    "--metrics-recording-only", "--disable-default-apps", "--no-first-run", "--disable-setuid-sandbox",
                    "--hide-scrollbars", "--no-zygote", "--disable-notifications", "--disable-logging",
                    "--disable-permissions-api");

            LoggingPreferences logPrefs = new LoggingPreferences();
            /*
             * logPrefs.enable(LogType.BROWSER, Level.ALL);
             * logPrefs.enable(LogType.PROFILER, Level.ALL);
             * logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
             */
            chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            chromeOptions.setCapability("goog:loggingPrefs", logPrefs);


            List<String> vals = (List<String>) (pluginConfig.getOrDefault("chrome.options", chromedefaults));
            vals.add("--user-agent=" + getUserAgent());
            chromeOptions.addArguments(vals);
            System.out.println("Launching Chrome with these options: "+ vals);
            chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

            if (loc.equals("local")) {
                driver = new ChromeDriver(chromeOptions);
                driver.manage().timeouts().pageLoadTimeout(3600, TimeUnit.SECONDS);
            } else {
                driver = new RemoteWebDriver(new URL(loc), chromeOptions);
            }

            driver.manage().window().setSize(new Dimension(1920, 1080));
            driver.manage().window().maximize();
            LOG.info("The window size is: " + driver.manage().window().getSize());
        }

    }

    /**
     * Attempts starting the web driver a fixed number of times
     *
     * @@param attempts Max number of times to retry
     * @return boolean Whether or not the web driver succesfully started
     */
    private boolean startDriver(int attempts) {
        for (int i = 0; i < attempts; ++i) {
            LOG.info("Attempt #" + i + ": Starting web driver...");
            try {
                startDriver();
                return driver.getCurrentUrl() != null;
            } catch (Exception e) {
                LOG.info("Intercepted the following error when attempting to start the web driver");
                LOG.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    private boolean takeScreenshot() {
        return Boolean.parseBoolean(pluginConfig.getOrDefault("chrome.selenium.screenshotoncapture", "false").toString());
    }

    /**
     * Run a crawl of the given resource
     *
     * @param resource The resource which directs what URI to start crawling from,
     *                 and a few other crawl-based configs
     * @return FetchedData a stack of content succesfully fetched from the URI
     */
    @Override
    public FetchedData fetch(Resource resource) {
        long start = System.currentTimeMillis();
        if (!startDriver(10)) {
            throw new RuntimeException("Failed to start the web driver! FetcherChrome cannot continue!");
        }
        LOG.info("Chrome FETCHER {}", resource.getUrl());

        float elapsed = (System.currentTimeMillis() - start) / 1000;
        LOG.info("Web driver created in " + elapsed + "s!");

        FetchedData data = null;

        try {
            /*
             * In this plugin we will work on only HTML data If data is of any other data
             * type like image, pdf etc plugin will return client error so it can be fetched
             * using default Fetcher
             */
            String mimeType = contentType(resource.getUrl());
            if (mimeType != null && mimeType.contains("application/json")) {
                data = jsonCrawl(resource);
            } else if (mimeType != null && mimeType.contains("text/html")) {
                data = htmlCrawl(resource);
            } else if(mimeType != null && mimeType.contains("text/plain")){
                //Some websites are junk and return text plain.
                data = htmlCrawl(resource);
            } else {
                LOG.warn("The mime type `" + mimeType + "` is not supported for crawling!");
                data = super.fetch(resource);
            }

            // Bind the crawl config to the reuslts
            data.setResource(resource);
        } catch (Exception e){
            Writer buffer = new StringWriter();
            PrintWriter pw = new PrintWriter(buffer);
            LOG.error(e.getMessage());
            e.printStackTrace(pw);
            LOG.error(buffer.toString());
            resource.setStatus(ResourceStatus.ERROR.toString());
        }
        finally {
            driver.quit();
            driver = null;

        }
        return data;

    }

    public FetchedData jsonCrawl(Resource resource) {
        JSONObject json = null;

        if (resource.getMetadata() != null && !resource.getMetadata().equals("")) {
            json = processMetadata(resource.getMetadata());

        }
        // This will block for the page load and any
        // associated AJAX requests
        do {
            try {
                driver.get(resource.getUrl());
                break;
            }
            catch (Exception e){
                try {
                    this.startDriver();
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), e);
                    ex.printStackTrace();
                }
            }
        }while(this.proxyEndpoints.size() > 0);

        resource.setStatus(ResourceStatus.FETCHED.toString());
            return new FetchedData(driver.getPageSource().getBytes(), "application/json", 200);

    }

    public FetchedData htmlCrawl(Resource resource) {
        JSONObject json = null;

        if (resource.getMetadata() != null && !resource.getMetadata().equals("")) {
            json = processMetadata(resource.getMetadata());

        }
        // This will block for the page load and any
        // associated AJAX requests

        do {
            try {
                driver.get(resource.getUrl());
                break;
            }
            catch (Exception e){
                try {
                    this.startDriver();
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        }while(this.proxyEndpoints.size() > 0);

        LOG.info("Driver started and ready on page: " + driver.getTitle(), "on url: " + driver.getCurrentUrl());

        String waitforready = pluginConfig.getOrDefault("chrome.selenium.javascriptready", "false").toString();

        if (waitforready.equals("true")) {
            new WebDriverWait(driver, 60).until((driver) -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").toString().equals("complete"));
        }
        Map<String, Object> tokens;
        Map script;
        if((pluginConfig.get("chrome.selenium.script") != null && pluginConfig.get("chrome.selenium.script") instanceof Map) ||
        json != null && json.containsKey("selenium")) {
            // Convert the raw JSONObject

            if((pluginConfig.get("chrome.selenium.script") != null)){
                tokens = (Map<String, Object>) pluginConfig.get("chrome.selenium.script");
            } else {
                tokens = (Map<String, Object>) json.get("selenium");
            }
            script = new TreeMap(tokens);

            // Determine the script type
            String versionToken = null;
            if(tokens.containsKey("version")){
                versionToken = tokens.get("version").toString();
            }
            if (versionToken == null) {
                LOG.warn("No `version` tag was specified, so `version: magnesium` will be infered!");

                versionToken = "magnesium";
            }
            versionToken = versionToken.toUpperCase();

            try {
                type = ScriptType.valueOf(versionToken);
            } catch (IllegalArgumentException e) {
                LOG.error("Invalid version: `" + versionToken + "`!");
                LOG.error("Exiting with null fetched results...");
                return null;
            }

            // Build the interpreter and run the script
            String snapshots;
            if (type == ScriptType.SELENIUM) {
                SeleniumScripter interpreter = new SeleniumScripter(driver);
                return runGuardedInterpreter(script, interpreter, resource);
            } else if (type == ScriptType.MAGNESIUM) {
                MagnesiumScript interpreter = new MagnesiumScript(driver);
                return runGuardedInterpreter(script, interpreter, resource);
            } else {
                throw new IllegalArgumentException("The version `" + type + "` is invalid! Must be one of: " + Arrays.toString(ScriptType.values()));
            }
        } else{
            resource.setStatus(ResourceStatus.FETCHED.toString());
            return new FetchedData(driver.getPageSource().getBytes(), "text/html", 200);
        }
    }

    /**
     * Feed a script into an interpreter and execute it.
     *
     * @param script      The script to run
     * @param interpreter The interpreter to use
     * @param resource    The crawl status tracker and configs
     */
    private FetchedData runGuardedInterpreter(Map script, SeleniumScripter interpreter, Resource resource) {
        LOG.info("Executing script with Ss interpreter: " + script);
        try {
            Path filepath = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(),
                    jobContext.getId());

            interpreter.setOutputPath(filepath.toString());
            interpreter.runScript(script);

            resource.setStatus(ResourceStatus.FETCHED.toString());
        } catch (Exception e) {
            // Print the original error
            LOG.error("Intercepted the following interpreter exception!");
            e.printStackTrace();

            // Dump all of the interpreter logs into the current stacktrace
            //dumpLogs();

            // Write all of the screenshots to persistence
            screenshot(interpreter);

            // Set the crawl status as errored
            resource.setStatus(ResourceStatus.ERROR.toString());
        }

        // Get the snapshots, if any, otherwise grap the current DOM content
        List<String> snapshots = interpreter.getSnapshots();
        if(snapshots.size() <= 0) {
            if(takeScreenshot()) {
                screenshot(interpreter);
            }
            snapshots.add(driver.getPageSource());
        }

        // Rasterize to a string and return it as FetchedData
        String rasterizedSnapshots = String.join(",", snapshots);
        return new FetchedData(rasterizedSnapshots.getBytes(), "text/html", 200);
    }

    /**
     * Feed a script into an interpreter and execute it.
     *
     * @param script      The script to run
     * @param interpreter The interpreter to use
     * @param resource    The crawl status tracker and configs
     */
    private FetchedData runGuardedInterpreter(Map script, MagnesiumScript interpreter, Resource resource) {
        LOG.info("Executing script with Ms interpreter: " + script);
        Program program = null;
        try {
            program = interpreter.interpret(script);

            resource.setStatus(ResourceStatus.FETCHED.toString());
        } catch (Exception e) {
            // Print the original error
            LOG.error("Intercepted the following interpreter exception!");
            e.printStackTrace();

            // Dump all of the interpreter logs into the current stacktrace
            //dumpLogs();

            // Write all of the screenshots to persistence
            screenshot(interpreter);

            // Set the crawl status as errored
            resource.setStatus(ResourceStatus.ERROR.toString());
        }

        // Get the snapshots, if any, otherwise grap the current DOM content
        List<String> snapshots = program.getSnapshots();
        if(snapshots.size() <= 0) {
            if(takeScreenshot()) {
                screenshot(interpreter);
            }
            snapshots.add(driver.getPageSource());
        }

        // Rasterize to a string and return it as FetchedData
        String rasterizedSnapshots = String.join(",", snapshots);
        return new FetchedData(rasterizedSnapshots.getBytes(), "text/html", 200);
    }

    /**
     * Takes a screenshot with a SeleniumScripter interpreter. The plugin config
     * `chrome.selenium.outputdirectory` must have a value for the screenshot
     * operation to be enabled
     *
     * @param interpreter The interpreter to take the screenshot with
     * @return boolean Whether or not a screenshot was succesfully taken
     */
    private boolean screenshot(SeleniumScripter interpreter) {
        try {
            if (pluginConfig.containsKey("chrome.selenium.outputdirectory")) {
                // Guarnetee the path to file exists and is open for writing
                Path path = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(),
                        jobContext.getId(), "errors");
                File f = path.toFile();
                f.mkdirs();

                // Take the screenshot
                Map<String, Object> screenshotOperation = new HashMap<>();
                screenshotOperation.put("targetdir", "errors");
                interpreter.screenshotOperation(screenshotOperation);

                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Takes a screenshot with a MagnesiumScript interpreter. The plugin config
     * `chrome.selenium.outputdirectory` must have a value for the screenshot
     * operation to be enabled
     *
     * @param interpreter The interpreter to take the screenshot with
     * @return boolean Whether or not a screenshot was succesfully taken
     */
    private boolean screenshot(MagnesiumScript interpreter) {
        try {
            if (pluginConfig.containsKey("chrome.selenium.outputdirectory")) {
                // Guarnetee the path to file exists and is open for writing
                Path path = Paths.get(pluginConfig.get("chrome.selenium.outputdirectory").toString(),
                        jobContext.getId(), "errors");
                File f = path.toFile();
                f.mkdirs();

                // Take the screenshot
                LOG.warn("Screenshot for Ms not implemented!");

                return false;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject processMetadata(String metadata) {
        if (metadata != null) {
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

    /**
     * Gets the content type of the given URI
     *
     * @param uri The URI to get the content type of
     * @return String the raw mime type of the given URI
     */
    private String contentType(String uri)  {
        try {
            CloseableHttpClient instance = HttpClients
                    .custom().setRedirectStrategy(new LaxRedirectStrategy())
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
            HttpGet g = new HttpGet(uri);
            HttpResponse response = instance.execute(g);
            HttpEntity entity = response.getEntity();
            ContentType ct = ContentType.getOrDefault(entity);
            if(ct != null) {
                return ct.getMimeType().toLowerCase();
            } else if(uri.endsWith(".htm") || uri.endsWith(".html")){
                return "text/html";
            } else if(uri.endsWith(".json")){
                return "application/json";
            }
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

