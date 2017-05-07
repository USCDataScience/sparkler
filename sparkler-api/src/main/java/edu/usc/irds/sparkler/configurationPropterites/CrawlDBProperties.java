package edu.usc.irds.sparkler.configurationPropterites;


import javax.validation.constraints.NotNull;
import java.net.URI;

public class CrawlDBProperties {
    @NotNull
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
