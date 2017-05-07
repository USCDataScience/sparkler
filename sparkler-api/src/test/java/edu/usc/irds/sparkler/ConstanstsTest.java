package edu.usc.irds.sparkler;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class ConstanstsTest {
    /**
     * Simple test of overriding the map
     */
    @Test
    public void maskingOfConfig() {
        Map<String, Object> defaultMap = new HashMap<String, Object>();
        Map<String, Object> overrideMap = new HashMap<>();
        defaultMap.put("temp", "value");
        overrideMap.put("temp", "newValue");
        assert (Constants.defaults.overrideConfigData(defaultMap, overrideMap).get("temp") == overrideMap.get("temp"));
    }
}
