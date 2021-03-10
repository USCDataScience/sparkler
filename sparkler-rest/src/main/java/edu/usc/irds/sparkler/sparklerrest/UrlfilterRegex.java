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
"urlfilter.regex.file"
})
public class UrlfilterRegex {

@JsonProperty("urlfilter.regex.file")
private String urlfilterRegexFile;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("urlfilter.regex.file")
public String getUrlfilterRegexFile() {
return urlfilterRegexFile;
}

@JsonProperty("urlfilter.regex.file")
public void setUrlfilterRegexFile(String urlfilterRegexFile) {
this.urlfilterRegexFile = urlfilterRegexFile;
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