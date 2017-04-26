package edu.usc.irds.sparkler.configUtils;

import org.hibernate.validator.constraints.NotEmpty;

public class Headers {
    @NotEmpty
    private String userAgent;
    @NotEmpty
    private String accept;
    @NotEmpty
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
