package edu.usc.irds.sparkler.config;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class FetcherProps {

    @Min(value = 1, message = "fetcher.serverDelay cannot be less than 1")
    @NotNull(message = "fetcher.serverDelay cannot be null")
    private int serverDelay;

    @Valid
    @NotNull(message = "fetcher.headers cannot be null")
    private Map<String, String> headers;

    @Valid
    private FileBackedList userAgents;

    public int getServerDelay() {
        return serverDelay;
    }

    public void setServerDelay(int serverDelay) {
        this.serverDelay = serverDelay;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public FileBackedList getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(FileBackedList userAgents) {
        this.userAgents = userAgents;
    }
}
