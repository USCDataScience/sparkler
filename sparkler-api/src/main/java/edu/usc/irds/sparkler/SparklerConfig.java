package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.configUtils.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @apiNote This class will help in parsing the configs for
 * Sparkler. This uses SNAKE YAML parser for parsing the config
 * structure present in conf folder.
 * @see org.yaml.snakeyaml.Yaml
 */
public class SparklerConfig implements BaseConfig {
    private CrawldbProps crawldb;
    private SparkProps spark;
    private KafkaProps kafka;
    private GenerateProps generate;
    private FetcherProps fetcher;
    private Map<String, PluginsProps> plugins;
    private List<String> activePlugins;

    private static class SparklerConfigConstructor extends Constructor {
        public SparklerConfigConstructor(Class type) {
            super(type);
            yamlClassConstructors.put(NodeId.mapping, new SparklerConfigConstruct());
        }

        class SparklerConfigConstruct extends Constructor.ConstructMapping {

            @Override
            protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
                Class type = node.getType();
                return super.constructJavaBean2ndStep(node, object);
            }
        }
    }

    public Object getPluginProps(String pluginId, Class<?> classToParse) {

        Yaml yaml = new Yaml();
        Map<String, PluginsProps> map = new LinkedHashMap<>();
        map.put(pluginId, plugins.get(pluginId));
        String dumpedData = yaml.dump(map);
        yaml = new Yaml(new Constructor(classToParse));
        System.out.println(yaml.load(dumpedData));
        return yaml.load(dumpedData);
    }

    private static class SparklerConfigRepresenter extends Representer {

    }

    private static class SparklerConfigDumperOpts extends DumperOptions {

    }

    private static class SparklerConfigLoaderOptions extends LoaderOptions {

    }

    public static SparklerConfig getSparklerConfig(InputStream inputStream) {
        SparklerConfigConstructor sparklerConfigConstructor = new SparklerConfigConstructor(SparklerConfig.class);
        SparklerConfigLoaderOptions sparklerConfigLoaderOptions = new SparklerConfigLoaderOptions();
        sparklerConfigLoaderOptions.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(sparklerConfigConstructor, new SparklerConfigRepresenter(), new SparklerConfigDumperOpts(), sparklerConfigLoaderOptions);
        return (SparklerConfig) yaml.load(inputStream);
    }

    public SparklerConfig() {

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

    public Map<String, PluginsProps> getPlugins() {
        return plugins;
    }

    public void setPlugins(Map<String, PluginsProps> plugins) {
        this.plugins = plugins;
    }

    public List<String> getActivePlugins() {
        return activePlugins;
    }

    public void setActivePlugins(List<String> activePlugins) {
        this.activePlugins = activePlugins;
    }
}
