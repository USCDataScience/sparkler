package edu.usc.irds.sparkler.configurationPropterites;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


/**
 * @apiNote This class is responsible for maintaining configs of JBrowser
 * plugin. Any updates to JBrowser config should be updated here.
 */
public class JBrowserProperties {
    @NotNull
    private boolean fetcherJBrowserEnable;

    @Min(value = 0, message = "socket.timeout should be non-negative")
    private int socketTimeout;


    @Min(value = 0, message = "connect.timeout should be non-negative")
    private int connectTimeout;


    public boolean isFetcherJBrowserEnable() {
        return fetcherJBrowserEnable;
    }

    public void setFetcherJBrowserEnable(boolean fetcherJBrowserEnable) {
        this.fetcherJBrowserEnable = fetcherJBrowserEnable;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public String toString() {
        return ("Socket Timeout:" + socketTimeout + " Connect Timeout:" + connectTimeout);
    }
}
