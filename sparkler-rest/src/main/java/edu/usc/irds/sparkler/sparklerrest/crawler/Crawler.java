package edu.usc.irds.sparkler.sparklerrest.crawler;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.pipeline.CrawlerRunner;

public class Crawler {

    SparklerConfiguration conf = Constants.defaults.newDefaultConfig();

    CrawlerRunner crawlerRunner = new CrawlerRunner();

    public void crawlJob(Object[] confoverride, String solrurl , String jobId){

        String[] s = new String[0];
        crawlerRunner.runCrawler(confoverride, "", jobId, 1000, 1, null, s, null, 10, 10, null, false, null, null, conf, "local[*]", "", s);

    }

}
