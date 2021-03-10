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
"kafka.enable",
"generate.groupby",
"spark.master",
"crawldb.uri",
"generate.top.groups",
"plugins",
"fetcher.headers",
"fetcher.server.delay",
"plugins.active",
"generate.topn",
"sparkler.conf.uuid",
"generate.sortby",
"databricks.enable",
"kafka.topic",
"kafka.listeners"
})
public class ConfigOverride {

@JsonProperty("kafka.enable")
private Boolean kafkaEnable;
@JsonProperty("generate.groupby")
private String generateGroupby;
@JsonProperty("spark.master")
private String sparkMaster;
@JsonProperty("crawldb.uri")
private String crawldbUri;
@JsonProperty("generate.top.groups")
private Double generateTopGroups;
@JsonProperty("plugins")
private Plugins plugins;
@JsonProperty("fetcher.headers")
private FetcherHeaders fetcherHeaders;
@JsonProperty("fetcher.server.delay")
private Double fetcherServerDelay;
@JsonProperty("plugins.active")
private List<String> pluginsActive = null;
@JsonProperty("generate.topn")
private Double generateTopn;
@JsonProperty("sparkler.conf.uuid")
private String sparklerConfUuid;
@JsonProperty("generate.sortby")
private String generateSortby;
@JsonProperty("databricks.enable")
private String databricksEnable;
@JsonProperty("kafka.topic")
private String kafkaTopic;
@JsonProperty("kafka.listeners")
private String kafkaListeners;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("kafka.enable")
public Boolean getKafkaEnable() {
return kafkaEnable;
}

@JsonProperty("kafka.enable")
public void setKafkaEnable(Boolean kafkaEnable) {
this.kafkaEnable = kafkaEnable;
}

@JsonProperty("generate.groupby")
public String getGenerateGroupby() {
return generateGroupby;
}

@JsonProperty("generate.groupby")
public void setGenerateGroupby(String generateGroupby) {
this.generateGroupby = generateGroupby;
}

@JsonProperty("spark.master")
public String getSparkMaster() {
return sparkMaster;
}

@JsonProperty("spark.master")
public void setSparkMaster(String sparkMaster) {
this.sparkMaster = sparkMaster;
}

@JsonProperty("crawldb.uri")
public String getCrawldbUri() {
return crawldbUri;
}

@JsonProperty("crawldb.uri")
public void setCrawldbUri(String crawldbUri) {
this.crawldbUri = crawldbUri;
}

@JsonProperty("generate.top.groups")
public Double getGenerateTopGroups() {
return generateTopGroups;
}

@JsonProperty("generate.top.groups")
public void setGenerateTopGroups(Double generateTopGroups) {
this.generateTopGroups = generateTopGroups;
}

@JsonProperty("plugins")
public Plugins getPlugins() {
return plugins;
}

@JsonProperty("plugins")
public void setPlugins(Plugins plugins) {
this.plugins = plugins;
}

@JsonProperty("fetcher.headers")
public FetcherHeaders getFetcherHeaders() {
return fetcherHeaders;
}

@JsonProperty("fetcher.headers")
public void setFetcherHeaders(FetcherHeaders fetcherHeaders) {
this.fetcherHeaders = fetcherHeaders;
}

@JsonProperty("fetcher.server.delay")
public Double getFetcherServerDelay() {
return fetcherServerDelay;
}

@JsonProperty("fetcher.server.delay")
public void setFetcherServerDelay(Double fetcherServerDelay) {
this.fetcherServerDelay = fetcherServerDelay;
}

@JsonProperty("plugins.active")
public List<String> getPluginsActive() {
return pluginsActive;
}

@JsonProperty("plugins.active")
public void setPluginsActive(List<String> pluginsActive) {
this.pluginsActive = pluginsActive;
}

@JsonProperty("generate.topn")
public Double getGenerateTopn() {
return generateTopn;
}

@JsonProperty("generate.topn")
public void setGenerateTopn(Double generateTopn) {
this.generateTopn = generateTopn;
}

@JsonProperty("sparkler.conf.uuid")
public String getSparklerConfUuid() {
return sparklerConfUuid;
}

@JsonProperty("sparkler.conf.uuid")
public void setSparklerConfUuid(String sparklerConfUuid) {
this.sparklerConfUuid = sparklerConfUuid;
}

@JsonProperty("generate.sortby")
public String getGenerateSortby() {
return generateSortby;
}

@JsonProperty("generate.sortby")
public void setGenerateSortby(String generateSortby) {
this.generateSortby = generateSortby;
}

@JsonProperty("databricks.enable")
public String getDatabricksEnable() {
return databricksEnable;
}

@JsonProperty("databricks.enable")
public void setDatabricksEnable(String databricksEnable) {
this.databricksEnable = databricksEnable;
}

@JsonProperty("kafka.topic")
public String getKafkaTopic() {
return kafkaTopic;
}

@JsonProperty("kafka.topic")
public void setKafkaTopic(String kafkaTopic) {
this.kafkaTopic = kafkaTopic;
}

@JsonProperty("kafka.listeners")
public String getKafkaListeners() {
return kafkaListeners;
}

@JsonProperty("kafka.listeners")
public void setKafkaListeners(String kafkaListeners) {
this.kafkaListeners = kafkaListeners;
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