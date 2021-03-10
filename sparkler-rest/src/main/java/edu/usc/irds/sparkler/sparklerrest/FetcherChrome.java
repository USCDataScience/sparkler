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
"chrome.wait.timeout",
"chrome.wait.element",
"chrome.wait.type",
"chrome.dns"
})
public class FetcherChrome {

@JsonProperty("chrome.wait.timeout")
private Integer chromeWaitTimeout;
@JsonProperty("chrome.wait.element")
private String chromeWaitElement;
@JsonProperty("chrome.wait.type")
private String chromeWaitType;
@JsonProperty("chrome.dns")
private String chromeDns;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("chrome.wait.timeout")
public Integer getChromeWaitTimeout() {
return chromeWaitTimeout;
}

@JsonProperty("chrome.wait.timeout")
public void setChromeWaitTimeout(Integer chromeWaitTimeout) {
this.chromeWaitTimeout = chromeWaitTimeout;
}

@JsonProperty("chrome.wait.element")
public String getChromeWaitElement() {
return chromeWaitElement;
}

@JsonProperty("chrome.wait.element")
public void setChromeWaitElement(String chromeWaitElement) {
this.chromeWaitElement = chromeWaitElement;
}

@JsonProperty("chrome.wait.type")
public String getChromeWaitType() {
return chromeWaitType;
}

@JsonProperty("chrome.wait.type")
public void setChromeWaitType(String chromeWaitType) {
this.chromeWaitType = chromeWaitType;
}

@JsonProperty("chrome.dns")
public String getChromeDns() {
return chromeDns;
}

@JsonProperty("chrome.dns")
public void setChromeDns(String chromeDns) {
this.chromeDns = chromeDns;
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