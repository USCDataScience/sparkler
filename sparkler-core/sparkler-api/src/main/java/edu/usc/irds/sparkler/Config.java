package edu.usc.irds.sparkler;

import java.util.List;

public interface Config extends ExtensionPoint {
    public List<String> processConfig();    
}
