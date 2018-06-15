package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.util.TestUtils;
import java.io.InputStream;
import java.net.URL;
import org.apache.tika.metadata.Metadata;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tg on 12/19/17.
 */
public class MyPluginTest {

    @Test
    public void testAdd() throws Exception {

        // Dummy Test case for plugin method
        HtmlParser plugin = TestUtils.newInstance(HtmlParser.class, "pluginId");

        String url = "https://en.wikipedia.org/wiki/Main_Page";
        String charsetName = "UTF-8";
        InputStream content = new URL(url).openStream();
        Metadata metadata = new Metadata();

        metadata.set("resourceName", url);
        metadata.set("Content-Encoding", charsetName);
        plugin.parseMetadata(content, metadata);
    }
}