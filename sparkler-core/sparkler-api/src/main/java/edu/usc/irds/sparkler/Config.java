package edu.usc.irds.sparkler;

import java.util.Collection;

public interface Config extends ExtensionPoint {
    public Collection<UrlInjectorObj> processConfig(Collection<String> urls);    
}
