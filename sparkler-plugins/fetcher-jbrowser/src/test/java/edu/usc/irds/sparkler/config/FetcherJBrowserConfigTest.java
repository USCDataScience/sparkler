package edu.usc.irds.sparkler.config;

import edu.usc.irds.sparkler.Constants;
import org.junit.Test;

public class FetcherJBrowserConfigTest {

    @Test
    public void test() throws Exception {
        SparklerConfig config = Constants.defaults.newDefaultConfig();
        FetcherJBrowserProps fetcherJBrowserProps = FetcherJBrowserProps.getFetcherJBrowserProps(config);
        fetcherJBrowserProps.validateFetcherJBrowserProps();
    }
}
