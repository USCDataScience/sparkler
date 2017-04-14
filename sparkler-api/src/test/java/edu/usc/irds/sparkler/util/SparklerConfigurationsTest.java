package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.SparklerConfigurations;
import edu.usc.irds.sparkler.configUtils.PluginsProps;
import org.junit.Test;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @apiNote This test suit will test the parsing of
 * sparkler configuration files.
 */
public class SparklerConfigurationsTest {
    @Test
    public void test() throws Exception {

        InputStream input = new FileInputStream(new File("../conf/Sparkler-default.yaml"));
        SparklerConfigConstructor constructor = new SparklerConfigConstructor(SparklerConfigurations.class);
        TypeDescription typeDescription = new TypeDescription(PluginsProps.class);
        constructor.addTypeDescription(typeDescription);
        Yaml yaml = new Yaml(constructor);
        SparklerConfigurations sparklerConfiguration = (SparklerConfigurations) yaml.load(input);
        System.out.println(sparklerConfiguration.getPlugins().getUrlFilter().getRegexFile());
    }
}

class SparklerConfigConstructor extends Constructor {
    public SparklerConfigConstructor(Class type) {
        super(type);
        yamlClassConstructors.put(NodeId.mapping, new SparklerConfigConstruct());
    }

    class SparklerConfigConstruct extends Constructor.ConstructMapping {
        @Override
        protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
            Class type = node.getType();
            if (type.equals(PluginsProps.class)) {
                SequenceNode activePlugins = null;
                for (NodeTuple valueNodeTuple : node.getValue()) {
                    ScalarNode tempNode = (ScalarNode) valueNodeTuple.getKeyNode();
                    if (tempNode.getValue().equals("active")) {
                        activePlugins = (SequenceNode) valueNodeTuple.getValueNode();
                        break;
                    }
                }
                Set<String> pluginsSet = new HashSet<>();
                for (Node valueNode : activePlugins.getValue()) {
                    pluginsSet.add(((ScalarNode) valueNode).getValue());
                }
                pluginsSet.add("active");
                ArrayList<NodeTuple> arrayList = new ArrayList<>();
                for (NodeTuple valuePluginsList : node.getValue()) {
                    ScalarNode tempNode = (ScalarNode) valuePluginsList.getKeyNode();
                    if (pluginsSet.contains(tempNode.getValue())) {
                        arrayList.add(valuePluginsList);
                    }
                }
                node.setValue(arrayList);
                return super.constructJavaBean2ndStep(node, object);
            } else {
                return super.constructJavaBean2ndStep(node, object);
            }
        }
    }
}