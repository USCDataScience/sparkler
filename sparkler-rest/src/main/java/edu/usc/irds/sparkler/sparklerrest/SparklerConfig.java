package edu.usc.irds.sparkler.sparklerrest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"urls",
"configOverride"
})
public class SparklerConfig {

@JsonProperty("urls")
private List<String> urls = null;
@JsonProperty("configOverride")
private ConfigOverride configOverride;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("urls")
public List<String> getUrls() {
return urls;
}

@JsonProperty("urls")
public void setUrls(List<String> urls) {
this.urls = urls;
}

@JsonProperty("configOverride")
public ConfigOverride getConfigOverride() {
return configOverride;
}

@JsonProperty("configOverride")
public void setConfigOverride(ConfigOverride configOverride) {
this.configOverride = configOverride;
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