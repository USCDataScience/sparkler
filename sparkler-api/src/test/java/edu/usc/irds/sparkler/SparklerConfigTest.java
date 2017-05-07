package edu.usc.irds.sparkler;


import edu.usc.irds.sparkler.configurationPropterites.*;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparklerConfigTest {
    @Test
    public void testSparklerConfigDump() throws Exception {
        SparklerConfig sparklerConfig = new SparklerConfig();
        CrawlDBProperties crawlDBProperties = new CrawlDBProperties();
        SparkProperties sparkProperties = new SparkProperties();
        KafkaProperties kafkaProperties = new KafkaProperties();
        GenerateProperties generateProperties = new GenerateProperties();
        FetcherProperties fetcherProperties = new FetcherProperties();
        Map<String, String> map = new HashMap<>();
        UrlFiltersRegexProperties urlFiltersRegexProperties = new UrlFiltersRegexProperties();
        map.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Sparkler/${project.version}");
        map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("Accept-Language", "en-US,en");
        JBrowserProperties jBrowserProperties = new JBrowserProperties();

        try {
            crawlDBProperties.setUri(new URI("http://localhost:8983/solr/crawldb"));
            sparkProperties.setSparkMaster("local[*]");
            kafkaProperties.setKafkaEnable(false);
            kafkaProperties.setKafkaTopic("sparkler_%s");
            kafkaProperties.setKafkaListeners(new URI("localhost:9092"));
            generateProperties.setGenerateTopGroups(256);
            generateProperties.setGenerateTopn(1000);
            fetcherProperties.setServerDelay(1000);
            fetcherProperties.setHeaders(map);
            urlFiltersRegexProperties.setUrlfilterRegexEnable(true);
            urlFiltersRegexProperties.setUrlfilterRegexFile("regex-urlfilter.txt");
            jBrowserProperties.setFetcherJBrowserEnable(true);
            jBrowserProperties.setConnectTimeout(3000);
            jBrowserProperties.setSocketTimeout(3000);
            sparklerConfig.setCrawlDBProperties(crawlDBProperties);
            sparklerConfig.setFetcherProperties(fetcherProperties);
            sparklerConfig.setGenerateProperties(generateProperties);
            sparklerConfig.setjBrowserProperties(jBrowserProperties);
            sparklerConfig.setKafkaProperties(kafkaProperties);
            sparklerConfig.setSparkProperties(sparkProperties);
            sparklerConfig.setUrlFiltersRegexProperties(urlFiltersRegexProperties);
            System.out.println(sparklerConfig.dump(sparklerConfig));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testRead() {
        String s = "!Sparkler\n" +
                "crawlDBProperties:\n uri: 'http://localhost:8983/solr/crawldb'\n" +
                "fetcherProperties:\n" +
                "  headers: {Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',\n" +
                "    User-Agent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36\n" +
                "      (KHTML, like Gecko) Sparkler/${project.version}', Accept-Language: 'en-US,en'}\n" +
                "  serverDelay: 1000\n" +
                "  userAgents: null\n" +
                "generateProperties: {generateTopGroups: 256, generateTopn: 1000}\n" +
                "jBrowserProperties: {connectTimeout: 3000, fetcherJBrowserEnable: true, socketTimeout: 3000}\n" +
                "kafkaProperties: {kafkaEnable: false, kafkaListeners: 'localhost:9092', kafkaTopic: sparkler_%s}\n" +
                "pluginsActive: null\n" +
                "pluginsBundleDirectory: null\n" +
                "sparkProperties: {sparkMaster: 'local[*]'}\n" +
                "urlFiltersRegexProperties: {urlfilterRegexEnable: true, urlfilterRegexFile: regex-urlfilter.txt}";
        SparklerConfig sparklerConfig = SparklerConfig.load(s);
        Assert.assertEquals(sparklerConfig.getCrawlDBProperties().getUri().toString(), "http://localhost:8983/solr/crawldb");

    }

}