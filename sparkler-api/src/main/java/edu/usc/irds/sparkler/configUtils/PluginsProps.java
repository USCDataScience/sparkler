package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;

import java.util.ArrayList;

public class PluginsProps implements BaseConfig {
    private ArrayList<String> active;
    private UrlFilterProps urlFilter;
    private FetcherJBrowser fetcherJbrowser;

    public ArrayList<String> getActive() {
        return active;
    }

    public void setActive(ArrayList<String> active) {
        this.active = active;
    }

    public UrlFilterProps getUrlFilter() {
        return urlFilter;
    }

    public void setUrlFilter(UrlFilterProps urlFilter) {
        this.urlFilter = urlFilter;
    }

    public FetcherJBrowser getFetcherJbrowser() {
        return fetcherJbrowser;
    }

    public void setFetcherJbrowser(FetcherJBrowser fetcherJbrowser) {
        this.fetcherJbrowser = fetcherJbrowser;
    }
}
