package edu.usc.irds.sparkler;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SparklerConfigurationTest {
    /**
     * If no configuration map is provided to the
     * SparklerConfiguration class constructor then a
     * Runtime Exception should be thrown.
     */
    @Test(expected = RuntimeException.class)
    public void testEmptyConfiguration() {
        SparklerConfiguration sparklerConfiguration = new SparklerConfiguration();
    }

    /**
     * Test in which an empty map is passed. This should return
     * RuntimeException showing all the keys which are required.
     * This will not do the validations part.
     */
    @Test
    public void passingEmptyMap() {
        try {
            SparklerConfiguration sparklerConfiguration = new SparklerConfiguration(new HashMap<>());
        } catch (Exception e) {
            assert (e.getClass() == RuntimeException.class);
        }
    }

    /**
     * This variable passes wrong inputs in map and will be passed
     * if the Runtime exception is thrown by the system.
     */
    @Test
    public void passAllVariablesButWrongValues() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("crawldb.uri", "/solr/crawldb");
        map.put("spark.master", "local[*]");
        map.put("generate.topn", "-1");
        map.put("generate.top.groups", "-1");
        map.put("fetcher.server.delay", "-1");
        map.put("plugins.bundle.directory", "asdfas");
        map.put("kafka.enable", "false");
        map.put("plugins.active", new ArrayList<>());
        try {
            SparklerConfiguration sparklerConfiguration = new SparklerConfiguration(map);
        } catch (Exception e) {
            assert (e.getClass() == RuntimeException.class);
        }
    }

    /**
     * Plugin properties are not satisfied but enabled.
     * This should also throw runtime exception.
     */
    @Test
    public void pluginPropertiesNotSpecified() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("crawldb.uri", "http://localhost:8983/solr/crawldb");
        map.put("spark.master", "local[*]");
        map.put("generate.topn", "-1");
        map.put("generate.top.groups", "-1");
        map.put("fetcher.server.delay", "-1");
        map.put("plugins.bundle.directory", "asdfas");
        map.put("kafka.enable", "true");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("urlfilter.regex");
        arrayList.add("fetcher.jbrowser");
        map.put("plugins.active", arrayList);
        try {
            SparklerConfiguration sparklerConfiguration = new SparklerConfiguration(map);
        } catch (Exception e) {
            assert (e.getClass() == RuntimeException.class);
        }
    }

    /**
     * All correct inputs passed
     */
    @Test
    public void everythingRight() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("crawldb.uri", "http://localhost:8983/solr/crawldb");
        map.put("spark.master", "local[*]");
        map.put("generate.topn", "1000");
        map.put("generate.top.groups", "256");
        map.put("fetcher.server.delay", "200");
        map.put("plugins.bundle.directory", "../sparkler-app/bundles");
        map.put("kafka.enable", "false");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("urlfilter.regex");
        arrayList.add("fetcher.jbrowser");
        map.put("plugins.active", arrayList);
        Map<String, Object> pluginsMap = new HashMap<String, Object>();
        Map<String, Object> urlFilterMap = new HashMap<>();
        urlFilterMap.put("urlfilter.regex.file", "regex-urlfilter.txt");
        pluginsMap.put("urlfilter.regex", urlFilterMap);
        Map<String, Object> jBrowserMap = new HashedMap();
        jBrowserMap.put("socket.timeout", 1000);
        jBrowserMap.put("connect.timeout", 1000);
        pluginsMap.put("fetcher.jbrowser", jBrowserMap);
        map.put("plugins", pluginsMap);
        SparklerConfiguration sparklerConfiguration = new SparklerConfiguration(map);
    }
}