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
"mode",
"values"
})
public class UrlInjector {

@JsonProperty("mode")
private String mode;
@JsonProperty("values")
private List<String> values = null;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("mode")
public String getMode() {
return mode;
}

@JsonProperty("mode")
public void setMode(String mode) {
this.mode = mode;
}

@JsonProperty("values")
public List<String> getValues() {
return values;
}

@JsonProperty("values")
public void setValues(List<String> values) {
this.values = values;
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