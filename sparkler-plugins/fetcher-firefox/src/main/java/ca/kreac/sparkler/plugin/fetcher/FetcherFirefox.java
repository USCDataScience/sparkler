/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.kreac.sparkler.plugin.fetcher;

import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import edu.usc.irds.sparkler.util.FetcherDefault;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.pf4j.Extension;

/**
 *
 * @author michelad
 */
@Extension
public class FetcherFirefox extends FetcherDefault {

    private LinkedHashMap<String, Object> pluginConfig;
//    private JBrowserDriver driver;
    private WebDriver driver;
    private FirefoxBinary firefoxBinary;
    private FirefoxOptions firefoxOptions;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        SparklerConfiguration config = jobContext.getConfiguration();
        pluginConfig = config.getPluginConfiguration(pluginId);
        
        File geckodriver = new File((String) pluginConfig.get("geckodriver"));
        if(!geckodriver.canExecute())
            geckodriver.setExecutable(true, false);
            
        System.setProperty("webdriver.gecko.driver", geckodriver.getPath());
        firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--headless");
        driver = new FirefoxDriver(firefoxOptions);
    }

    @Override
    public FetchedData fetch(Resource resource) throws Exception {
        LOG.info("Firefox FETCHER {}", resource.getUrl());
        FetchedData fetchedData;

        String url = resource.getUrl();
        if (!isWebPage(url)) {
            LOG.debug("{} not a html. Falling back to default fetcher.", resource.getUrl());
            //This should be true for all URLS ending with 4 character file extension
            //return new FetchedData("".getBytes(), "application/html", ERROR_CODE) ;
            return super.fetch(resource);
        }

        long start = System.currentTimeMillis();

        LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));

        driver.get(url);
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);

        String html = driver.getPageSource();
        
        LOG.debug("Time taken to load {} - {} ", resource.getUrl(), (System.currentTimeMillis() - start));

        fetchedData = new FetchedData();
        fetchedData.setContent(html.getBytes());
        fetchedData.setContentType("application/html");
        resource.setStatus(ResourceStatus.FETCHED.toString());
        fetchedData.setFetchedAt(new Date());
        fetchedData.setResource(resource);

        driver.quit();

        return fetchedData;
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
}
