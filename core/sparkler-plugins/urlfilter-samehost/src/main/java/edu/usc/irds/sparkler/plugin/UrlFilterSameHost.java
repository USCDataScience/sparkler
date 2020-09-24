package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.URLFilter;
import org.pf4j.Extension;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tg on 12/19/17.
 * URL filter plugin to restrict to the same host
 */
@Extension
public class UrlFilterSameHost extends AbstractExtensionPoint implements URLFilter {

    @Override
    public boolean filter(String child, String parent) {
        try {
           return new URL(child).getHost().equals(new URL(parent).getHost());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
