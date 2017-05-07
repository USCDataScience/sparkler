package edu.usc.irds.sparkler.configurationPropterites;


import javax.validation.constraints.NotNull;

/**
 * @apiNote This class is responsible for storing config properties of
 * URL filters. Any change related to plugin should be updated
 * here.
 */
public class UrlFiltersRegexProperties {
    @NotNull
    private boolean urlfilterRegexEnable;

    private String urlfilterRegexFile;

    public boolean isUrlfilterRegexEnable() {
        return urlfilterRegexEnable;
    }

    public void setUrlfilterRegexEnable(boolean urlfilterRegexEnable) {
        this.urlfilterRegexEnable = urlfilterRegexEnable;
    }

    public String getUrlfilterRegexFile() {
        return urlfilterRegexFile;
    }

    public void setUrlfilterRegexFile(String urlfilterRegexFile) {
        this.urlfilterRegexFile = urlfilterRegexFile;
    }

    @Override
    public String toString() {
        return ("URL Filter Regex File:" + this.urlfilterRegexFile);
    }
}