package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;

import javax.validation.constraints.NotNull;
import java.net.URL;

public class CrawldbProps implements BaseConfig {
    @NotNull(message = "crawldb.url cannot be null")
    private URL url;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
