package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.config.SparklerConfig;
import org.junit.Test;

/**
 * @apiNote This test suit will test the parsing of
 * sparkler configuration files.
 */
public class SparklerConfigTest {
    @Test
    public void test() throws Exception {
        SparklerConfig sparklerConfig = Constants.defaults.newDefaultSparklerConfig();
        sparklerConfig.validateSparklerConfig();
    }
}
