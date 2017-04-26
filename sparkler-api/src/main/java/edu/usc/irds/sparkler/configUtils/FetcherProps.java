package edu.usc.irds.sparkler.configUtils;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class FetcherProps {
    @Max(1)
    @NotNull
    private int serverDelay;
    @Valid
    @NotNull
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
