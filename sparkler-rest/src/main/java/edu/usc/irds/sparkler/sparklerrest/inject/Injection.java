package edu.usc.irds.sparkler.sparklerrest.inject;

public class Injection {
    private String[] urls;
    private String crawldb;
    private Object[] configOverride;

    public Injection(){

    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String getCrawldb() {
        return crawldb;
    }

    public void setCrawldb(String crawldb) {
        this.crawldb = crawldb;
    }

    public Object[] getConfigOverride() {
        return configOverride;
    }

    public void setConfigOverride(Object[] configOverride) {
        this.configOverride = configOverride;
    }
}
