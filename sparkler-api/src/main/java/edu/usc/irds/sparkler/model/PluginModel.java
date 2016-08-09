package edu.usc.irds.sparkler.model;


import java.io.Serializable;

public class PluginModel implements Serializable {

    RegexUrlFilterModel regexUrlFilter;

    public RegexUrlFilterModel getRegexUrlFilter() {
        return regexUrlFilter;
    }

    public void setRegexUrlFilter(RegexUrlFilterModel regexUrlFilter) {
        this.regexUrlFilter = regexUrlFilter;
    }

    public void mask(PluginModel other) {
        if (regexUrlFilter != null) regexUrlFilter.mask(other.regexUrlFilter);
    }
}
