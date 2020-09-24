package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tg on 12/19/17.
 */
public class UrlFilterSameHostTest {


    @Test
    public void filter() throws Exception {
        UrlFilterSameHost plugin = TestUtils.newInstance(UrlFilterSameHost.class, "urlfilter-samehost");
        String urls[] = {
          "http://host1.com/a",
          "http://host1.com/b",
          "http://host2.com/a",
        };

        assertTrue(plugin.filter(urls[0], urls[1]));
        assertFalse(plugin.filter(urls[0], urls[2]));
    }

}