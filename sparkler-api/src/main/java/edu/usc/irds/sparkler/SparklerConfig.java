package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.configurationPropterites.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.validation.Valid;


import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class SparklerConfig implements BaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SparklerConfig.class);

    /*********************
     * Solr Related Config
     ********************/
    @Valid
    private CrawlDBProperties crawlDBProperties;

    /*********************
     * Apache Spark Config
     ********************/
    @Valid
    private SparkProperties sparkProperties;

    /*********************
     * Generate Properties
     ********************/
    @Valid
    private GenerateProperties generateProperties;

    /***************************
     * Fetcher Server Properties
     **************************/
    @Valid
    private FetcherProperties fetcherProperties;

    /****************
     * Plugins Config
     ***************/
    @NotNull(message = "plugins.bundle.directory should be provided")
    private File pluginsBundleDirectory;

    @NotNull(message = "plugins.active should be provided")
    private ArrayList<String> pluginsActive;

    /*********************
     * Apache Kafka Config
     ********************/
    @Valid
    private KafkaProperties kafkaProperties;

    /*******************
     * URL Filter Config
     ******************/
    @Valid
    private UrlFiltersRegexProperties urlFiltersRegexProperties;

    /*****************************
     * Fetcher JBrowser Properties
     ****************************/
    @Valid
    JBrowserProperties jBrowserProperties;

    private static final Set<Class<?>> STRING_SERIALIZABLE = new HashSet<>(Arrays.asList(
            URI.class, File.class, Path.class, URL.class));

    /*************************************************
     * Representation of SparklerConfig in YAML Format
     ************************************************/
    private static class SparklerConfigRepresenter extends Representer {
        SparklerConfigRepresenter() {
            super();
            this.addClassTag(SparklerConfig.class, new Tag("!Sparkler"));
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            NodeTuple tuple = super.representJavaBeanProperty(javaBean, property,
                    propertyValue, customTag);
            if (propertyValue != null && STRING_SERIALIZABLE.contains(property.getType())) {
                if (Object.class.equals(tuple.getValueNode().getType())) {
                    if (tuple.getValueNode().getClass().isAssignableFrom(MappingNode.class)
                            && ((MappingNode) tuple.getValueNode()).getValue().isEmpty()) {
                        Node value = representScalar(tuple.getKeyNode().getTag(), propertyValue.toString());
                        tuple = new NodeTuple(tuple.getKeyNode(), value);
                    }
                }
            }
            return tuple;
        }
    }

    private static final Yaml YAML;

    static {
        Constructor constructor = new Constructor();
        constructor.addTypeDescription(new TypeDescription(SparklerConfig.class, "!Sparkler"));
        YAML = new Yaml(constructor, new SparklerConfigRepresenter());
    }

    public static String dump(Object o) {
        synchronized (SparklerConfig.class) {
            return YAML.dump(o);
        }
    }

    public static SparklerConfig load(String s) {
        synchronized (SparklerConfig.class) {
            return (SparklerConfig) YAML.load(s);
        }
    }


    /*********************
     * Getters and Setters
     ********************/

    public CrawlDBProperties getCrawlDBProperties() {
        return crawlDBProperties;
    }

    public void setCrawlDBProperties(CrawlDBProperties crawlDBProperties) {
        this.crawlDBProperties = crawlDBProperties;
    }

    public SparkProperties getSparkProperties() {
        return sparkProperties;
    }

    public void setSparkProperties(SparkProperties sparkProperties) {
        this.sparkProperties = sparkProperties;
    }

    public GenerateProperties getGenerateProperties() {
        return generateProperties;
    }

    public void setGenerateProperties(GenerateProperties generateProperties) {
        this.generateProperties = generateProperties;
    }

    public FetcherProperties getFetcherProperties() {
        return fetcherProperties;
    }

    public void setFetcherProperties(FetcherProperties fetcherProperties) {
        this.fetcherProperties = fetcherProperties;
    }

    public File getPluginsBundleDirectory() {
        return pluginsBundleDirectory;
    }

    public void setPluginsBundleDirectory(File pluginsBundleDirectory) {
        this.pluginsBundleDirectory = pluginsBundleDirectory;
    }

    public ArrayList<String> getPluginsActive() {
        return pluginsActive;
    }

    public void setPluginsActive(ArrayList<String> pluginsActive) {
        this.pluginsActive = pluginsActive;
    }

    public KafkaProperties getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public UrlFiltersRegexProperties getUrlFiltersRegexProperties() {
        return urlFiltersRegexProperties;
    }

    public void setUrlFiltersRegexProperties(UrlFiltersRegexProperties urlFiltersRegexProperties) {
        this.urlFiltersRegexProperties = urlFiltersRegexProperties;
    }

    public JBrowserProperties getjBrowserProperties() {
        return jBrowserProperties;
    }

    public void setjBrowserProperties(JBrowserProperties jBrowserProperties) {
        this.jBrowserProperties = jBrowserProperties;
    }
}
