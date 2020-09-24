package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tg on 12/19/17.
 */
public class MyPluginTest {


    @Test
    public void testAdd() throws Exception {
        //Dummy Test case for plugin method
        MyPlugin plugin = TestUtils.newInstance(MyPlugin.class, "pluginId");
        assertEquals(3, plugin.add(1, 2));
        assertEquals(-1, plugin.add(1, -2));
    }
}