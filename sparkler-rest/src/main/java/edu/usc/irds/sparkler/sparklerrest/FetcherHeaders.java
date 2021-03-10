package edu.usc.irds.sparkler.sparklerrest;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"User-Agent",
"Accept",
"Accept-Language",
"Origin",
"Authorization",
"Cache-Control",
"Upgrade-Insecure-Requests",
"Referer",
"Connection",
"Host",
"Accept-Encoding",
"Content-Length",
"Content-Type"
})
public class FetcherHeaders {

@JsonProperty("User-Agent")
private String userAgent;
@JsonProperty("Accept")
private String accept;
@JsonProperty("Accept-Language")
private String acceptLanguage;
@JsonProperty("Origin")
private String origin;
@JsonProperty("Authorization")
private String authorization;
@JsonProperty("Cache-Control")
private String cacheControl;
@JsonProperty("Upgrade-Insecure-Requests")
private String upgradeInsecureRequests;
@JsonProperty("Referer")
private String referer;
@JsonProperty("Connection")
private String connection;
@JsonProperty("Host")
private String host;
@JsonProperty("Accept-Encoding")
private String acceptEncoding;
@JsonProperty("Content-Length")
private String contentLength;
@JsonProperty("Content-Type")
private String contentType;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("User-Agent")
public String getUserAgent() {
return userAgent;
}

@JsonProperty("User-Agent")
public void setUserAgent(String userAgent) {
this.userAgent = userAgent;
}

@JsonProperty("Accept")
public String getAccept() {
return accept;
}

@JsonProperty("Accept")
public void setAccept(String accept) {
this.accept = accept;
}

@JsonProperty("Accept-Language")
public String getAcceptLanguage() {
return acceptLanguage;
}

@JsonProperty("Accept-Language")
public void setAcceptLanguage(String acceptLanguage) {
this.acceptLanguage = acceptLanguage;
}

@JsonProperty("Origin")
public String getOrigin() {
return origin;
}

@JsonProperty("Origin")
public void setOrigin(String origin) {
this.origin = origin;
}

@JsonProperty("Authorization")
public String getAuthorization() {
return authorization;
}

@JsonProperty("Authorization")
public void setAuthorization(String authorization) {
this.authorization = authorization;
}

@JsonProperty("Cache-Control")
public String getCacheControl() {
return cacheControl;
}

@JsonProperty("Cache-Control")
public void setCacheControl(String cacheControl) {
this.cacheControl = cacheControl;
}

@JsonProperty("Upgrade-Insecure-Requests")
public String getUpgradeInsecureRequests() {
return upgradeInsecureRequests;
}

@JsonProperty("Upgrade-Insecure-Requests")
public void setUpgradeInsecureRequests(String upgradeInsecureRequests) {
this.upgradeInsecureRequests = upgradeInsecureRequests;
}

@JsonProperty("Referer")
public String getReferer() {
return referer;
}

@JsonProperty("Referer")
public void setReferer(String referer) {
this.referer = referer;
}

@JsonProperty("Connection")
public String getConnection() {
return connection;
}

@JsonProperty("Connection")
public void setConnection(String connection) {
this.connection = connection;
}

@JsonProperty("Host")
public String getHost() {
return host;
}

@JsonProperty("Host")
public void setHost(String host) {
this.host = host;
}

@JsonProperty("Accept-Encoding")
public String getAcceptEncoding() {
return acceptEncoding;
}

@JsonProperty("Accept-Encoding")
public void setAcceptEncoding(String acceptEncoding) {
this.acceptEncoding = acceptEncoding;
}

@JsonProperty("Content-Length")
public String getContentLength() {
return contentLength;
}

@JsonProperty("Content-Length")
public void setContentLength(String contentLength) {
this.contentLength = contentLength;
}

@JsonProperty("Content-Type")
public String getContentType() {
return contentType;
}

@JsonProperty("Content-Type")
public void setContentType(String contentType) {
this.contentType = contentType;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}