/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.irds.sparkler.config;

import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.SparklerRuntimeException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.representer.Representer;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @apiNote This class will help in parsing the configs for
 * Sparkler. This uses SNAKE YAML parser for parsing the config
 * structure present in conf folder. The validations of the config
 * are done using hibernate-validator
 * @see org.yaml.snakeyaml.Yaml
 * @see org.hibernate.validator
 */
public class SparklerConfig implements BaseConfig {
    /****************************
     * CONFIGURATIONS OF SPARKLER
     ***************************/
    @NotNull(message = "crawldb properties cannot be null")
    @Valid
    private CrawldbProps crawldb;
    @NotNull(message = "spark properties cannot be null")
    @Valid
    private SparkProps spark;
    @NotNull(message = "kafka properties cannot be null")
    @Valid
    private KafkaProps kafka;
    @NotNull(message = "generate properties cannot be null")
    @Valid
    private GenerateProps generate;
    @NotNull(message = "fetcher properties cannot be null")
    @Valid
    private FetcherProps fetcher;
    @NotNull(message = "plugins cannot be null")
    @Valid
    private Map<String, PluginProps> plugins;

    @NotNull(message = "activePlugins list cannot be null")
    @Valid
    private List<String> activePlugins;

    @NotNull(message = "pluginsDir should not be null")
    @Valid
    private String pluginsDir;

    /***************************
     * SNAKE-YAML configurations
     ***************************/
    private static class SparklerConfigConstructor extends Constructor {
        public SparklerConfigConstructor(Class type) {
            super(type);
            yamlClassConstructors.put(NodeId.mapping, new SparklerConfigConstruct());
        }

        class SparklerConfigConstruct extends Constructor.ConstructMapping {

            @Override
            protected Object constructJavaBean2ndStep(MappingNode node, Object object) {

                return super.constructJavaBean2ndStep(node, object);
            }
        }
    }

    private static class SparklerConfigRepresenter extends Representer {

    }

    private static class SparklerConfigDumperOpts extends DumperOptions {

    }

    private static class SparklerConfigLoaderOptions extends LoaderOptions {

    }

    /***************************************************************
     * @param pluginId     the plugin conf id that needs to be
     *                     parsed
     * @param classToParse class in which pluginId data needs to be
     *                     parsed
     * @return Object - Parsed into the respective class
     * @apiNote This function helps in second level plugin props
     * parsing. Pass the plugin Id and this utility function will
     * fetch from the plugins map and parse it into a plugin bean
     * class.
     **************************************************************/
    public <T> T getPluginProps(String pluginId, Class<T> classToParse) {

        if (!plugins.containsKey(pluginId)) {
            throw new SparklerRuntimeException("No plugin conf found for id:'"
                    + pluginId + "'. Available:" + plugins.keySet());
        }
        Yaml yaml = new Yaml();
        String dumpedData = yaml.dump(plugins.get(pluginId));
        yaml = new Yaml(new Constructor(classToParse));
        return (T) yaml.load(dumpedData);
    }

    /********************************************************************
     * @param inputStream Stream of data that needs to be parsed into
     *                    SparklerConfig object
     * @return SparklerConfig Object
     * @apiNote This function helps in parsing input stream to a sparkler
     * config object
     *******************************************************************/
    public static SparklerConfig getSparklerConfig(InputStream inputStream) {
        SparklerConfigConstructor sparklerConfigConstructor = new SparklerConfigConstructor(SparklerConfig.class);
        SparklerConfigLoaderOptions sparklerConfigLoaderOptions = new SparklerConfigLoaderOptions();
        sparklerConfigLoaderOptions.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(sparklerConfigConstructor,
                new SparklerConfigRepresenter(),
                new SparklerConfigDumperOpts(),
                sparklerConfigLoaderOptions);
        return (SparklerConfig) yaml.load(inputStream);
    }

    /***********************************************************
     * @param map that needs to be parsed into sparkler config
     *            object
     * @return SparklerConfig Object
     * @apiNote This function helps in parsing map to a sparkler
     * config object
     **********************************************************/
    public static SparklerConfig getSparklerConfig(Map<String, Object> map) {
        Yaml yaml = new Yaml();
        String dumpedData = yaml.dump(map);
        InputStream inputStream = new ByteArrayInputStream(dumpedData.getBytes(StandardCharsets.UTF_8));
        return getSparklerConfig(inputStream);
    }

    /**
     * @return true if the sparkler configuration object parsed is valid
     * @throws SparklerException
     * @apiNote This function validates the SparklerConfig Object
     */
    public Boolean validateSparklerConfig() throws SparklerException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SparklerConfig>> constraintViolations = validator.validate(this);
        for (ConstraintViolation constraintViolation : constraintViolations) {
            throw (new SparklerException(constraintViolation.getMessage()));
        }
        return true;
    }

    public SparklerConfig() {

    }

    /*****************************************
     * GETTERS AND SETTERS FOR SPARKLER-CONFIG
     ****************************************/
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

    public Map<String, PluginProps> getPlugins() {
        return plugins;
    }

    public void setPlugins(Map<String, PluginProps> plugins) {
        this.plugins = plugins;
    }

    public List<String> getActivePlugins() {
        return activePlugins;
    }

    public void setActivePlugins(List<String> activePlugins) {
        this.activePlugins = activePlugins;
    }

    public String getPluginsDir() {
        return pluginsDir;
    }

    public void setPluginsDir(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }
}
