package edu.usc.irds.sparkler.config;

import edu.usc.irds.sparkler.Constants;
import org.junit.Test;

public class UrlFilterPropsTest {
    private UrlFilterProps urlFilterProps;

    @Test
    public void test() throws Exception {
        SparklerConfig sparklerConfig = Constants.defaults.newDefaultSparklerConfig();
        UrlFilterProps urlFilterProps = UrlFilterProps.getUrlFilterProps(sparklerConfig);
        urlFilterProps.validateUrlFilterProps();
    }
}
