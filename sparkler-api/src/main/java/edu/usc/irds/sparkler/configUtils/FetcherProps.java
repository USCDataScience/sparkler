package edu.usc.irds.sparkler.configUtils;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class FetcherProps {
    @Min(value = 1, message = "fetcher.serverDelay cannot be less than 1")
    @NotNull(message = "fetcher.serverDelay cannot be null")
    private int serverDelay;
    @Valid
    @NotNull(message = "fetcher.headers cannot be null")
    private Headers headers;

    public int getServerDelay() {
        return serverDelay;
    }

    public void setServerDelay(int serverDelay) {
        this.serverDelay = serverDelay;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }
}
