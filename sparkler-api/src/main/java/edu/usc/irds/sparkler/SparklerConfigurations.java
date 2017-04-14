package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.configUtils.*;

/**
 * @apiNote This class will help in parsing the configs for
 * Sparkler. This uses SNAKE YAML parser for parsing the config
 * structure present in conf folder.
 * @see org.yaml.snakeyaml.Yaml
 */
public class SparklerConfigurations implements BaseConfig {
    private CrawldbProps crawldb;
    private SparkProps spark;
    private KafkaProps kafka;
    private GenerateProps generate;
    private FetcherProps fetcher;
    private PluginsProps plugins;

    public SparklerConfigurations() {

    }

    public CrawldbProps getCrawldb() {
        return crawldb;
    }

    public void setCrawldb(CrawldbProps crawldb) {
        this.crawldb = crawldb;
    }

    public SparkProps getSpark() {
        return spark;
    }

    public void setSpark(SparkProps spark) {
        this.spark = spark;
    }

    public KafkaProps getKafka() {
        return kafka;
    }

    public void setKafka(KafkaProps kafka) {
        this.kafka = kafka;
    }

    public GenerateProps getGenerate() {
        return generate;
    }

    public void setGenerate(GenerateProps generate) {
        this.generate = generate;
    }

    public FetcherProps getFetcher() {
        return fetcher;
    }

    public void setFetcher(FetcherProps fetcher) {
        this.fetcher = fetcher;
    }

    public PluginsProps getPlugins() {
        return plugins;
    }

    public void setPlugins(PluginsProps plugins) {
        this.plugins = plugins;
    }
}
