package edu.usc.irds.sparkler.configurationPropterites;


import org.hibernate.validator.constraints.Range;

import java.util.List;
import java.util.Map;

public class FetcherProperties {
    @Range(min = 1, max = 60 * 60 * 1000)
    private long serverDelay;

    private Map<String, String> headers;

    private List<String> userAgents;

    public long getServerDelay() {
        return serverDelay;
    }

    public void setServerDelay(long serverDelay) {
        this.serverDelay = serverDelay;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(List<String> userAgents) {
        this.userAgents = userAgents;
    }
}
