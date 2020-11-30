package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Config;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Extension
public class UrlInjector extends AbstractExtensionPoint implements Config {
    
    private static final Logger LOG = LoggerFactory.getLogger(UrlInjector.class);

    @Override
    public List<String> processConfig() {
        System.out.println("hello");
        LOG.debug("HELLO WORLD");
        ArrayList<String> s = new ArrayList<>();

        return s;
    }
}
