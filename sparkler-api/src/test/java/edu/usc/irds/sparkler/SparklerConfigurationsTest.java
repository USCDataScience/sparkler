package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.configUtils.PluginsProps;
import org.junit.Test;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;

/**
 * @apiNote This test suit will test the parsing of
 * sparkler configuration files.
 */
public class SparklerConfigurationsTest {
    @Test
    public void test() throws Exception {
        SparklerConfig sparklerConfig = Constants.defaults.newDefaultSparklerConfig();
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
                return null;
            } else {
                return super.constructJavaBean2ndStep(node, object);
            }
        }
    }
}