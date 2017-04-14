package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;

public class FetcherJBrowser implements BaseConfig {
    private int socketTimeout;
    private int connectTimeout;

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
}
