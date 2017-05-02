package edu.usc.irds.sparkler.config;

import edu.usc.irds.sparkler.Constants;
import org.junit.Test;

/**
 * @apiNote This test suit will test the parsing of
 * sparkler configuration files.
 */
public class SparklerConfigTest {

    @Test
    public void test() throws Exception {
        SparklerConfig sparklerConfig = Constants.defaults.newDefaultConfig();
        System.out.println(sparklerConfig.getFetcher().getUserAgents());
        sparklerConfig.validateSparklerConfig();
    }
}
