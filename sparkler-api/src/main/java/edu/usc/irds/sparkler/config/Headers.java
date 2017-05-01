package edu.usc.irds.sparkler.config;

import org.hibernate.validator.constraints.NotEmpty;

public class Headers {
    @NotEmpty(message = "fetcher.headers.userAgent cannot be null")
    private String userAgent;
    @NotEmpty(message = "fetcher.headers.accept cannot be null")
    private String accept;
    @NotEmpty(message = "fetcher.headers.acceptLanguage cannot be null")
    private String acceptLanguage;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }
}
