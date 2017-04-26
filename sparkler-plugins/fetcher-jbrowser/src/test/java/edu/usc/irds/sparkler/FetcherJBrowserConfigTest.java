package edu.usc.irds.sparkler;

import org.junit.Test;

public class FetcherJBrowserConfigTest {
    private FetcherJBrowserConfig fetcherJBrowser;

    @Test
    public void test1() {
        SparklerConfig sparklerConfig = Constants.defaults.newDefaultSparklerConfig();
        FetcherJBrowserConfig dumpedData = (FetcherJBrowserConfig) sparklerConfig.getPluginProps("fetcherJbrowser", FetcherJBrowserConfig.class);
        System.out.println(dumpedData.getFetcherJbrowser().getSocketTimeout());
    }
}
