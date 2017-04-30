package edu.usc.irds.sparkler.configUtils;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.SparklerConfig;
import org.junit.Test;

public class FetcherJBrowserConfigTest {

    @Test
    public void test() throws Exception {
        SparklerConfig config = Constants.defaults.newDefaultSparklerConfig();
        FetcherJBrowserProps fetcherJBrowserProps = FetcherJBrowserProps.getFetcherJBrowserProps(config);
        fetcherJBrowserProps.validateFetcherJBrowserProps();
    }
}
