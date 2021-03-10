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
"scorer.dd.svn.url",
"scorer.dd.svn.fallback",
"scorer.dd.svn.key"
})
public class ScorerDdSvn {

@JsonProperty("scorer.dd.svn.url")
private String scorerDdSvnUrl;
@JsonProperty("scorer.dd.svn.fallback")
private Integer scorerDdSvnFallback;
@JsonProperty("scorer.dd.svn.key")
private String scorerDdSvnKey;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("scorer.dd.svn.url")
public String getScorerDdSvnUrl() {
return scorerDdSvnUrl;
}

@JsonProperty("scorer.dd.svn.url")
public void setScorerDdSvnUrl(String scorerDdSvnUrl) {
this.scorerDdSvnUrl = scorerDdSvnUrl;
}

@JsonProperty("scorer.dd.svn.fallback")
public Integer getScorerDdSvnFallback() {
return scorerDdSvnFallback;
}

@JsonProperty("scorer.dd.svn.fallback")
public void setScorerDdSvnFallback(Integer scorerDdSvnFallback) {
this.scorerDdSvnFallback = scorerDdSvnFallback;
}

@JsonProperty("scorer.dd.svn.key")
public String getScorerDdSvnKey() {
return scorerDdSvnKey;
}

@JsonProperty("scorer.dd.svn.key")
public void setScorerDdSvnKey(String scorerDdSvnKey) {
this.scorerDdSvnKey = scorerDdSvnKey;
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