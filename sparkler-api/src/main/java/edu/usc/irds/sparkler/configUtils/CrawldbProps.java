package edu.usc.irds.sparkler.configUtils;

import edu.usc.irds.sparkler.BaseConfig;

import java.net.URI;

public class CrawldbProps implements BaseConfig {
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
