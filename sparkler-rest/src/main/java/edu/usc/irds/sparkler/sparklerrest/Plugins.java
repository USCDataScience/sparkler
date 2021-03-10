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
"urlfilter.regex",
"scorer.dd.svn",
"fetcher.jbrowser",
"fetcher.chrome",
"url.injector",
"memex.webpage.mimetype"
})
public class Plugins {

@JsonProperty("urlfilter.regex")
private UrlfilterRegex urlfilterRegex;
@JsonProperty("scorer.dd.svn")
private ScorerDdSvn scorerDdSvn;
@JsonProperty("fetcher.jbrowser")
private Object fetcherJbrowser;
@JsonProperty("fetcher.chrome")
private FetcherChrome fetcherChrome;
@JsonProperty("url.injector")
private UrlInjector urlInjector;
@JsonProperty("memex.webpage.mimetype")
private String memexWebpageMimetype;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("urlfilter.regex")
public UrlfilterRegex getUrlfilterRegex() {
return urlfilterRegex;
}

@JsonProperty("urlfilter.regex")
public void setUrlfilterRegex(UrlfilterRegex urlfilterRegex) {
this.urlfilterRegex = urlfilterRegex;
}

@JsonProperty("scorer.dd.svn")
public ScorerDdSvn getScorerDdSvn() {
return scorerDdSvn;
}

@JsonProperty("scorer.dd.svn")
public void setScorerDdSvn(ScorerDdSvn scorerDdSvn) {
this.scorerDdSvn = scorerDdSvn;
}

@JsonProperty("fetcher.jbrowser")
public Object getFetcherJbrowser() {
return fetcherJbrowser;
}

@JsonProperty("fetcher.jbrowser")
public void setFetcherJbrowser(Object fetcherJbrowser) {
this.fetcherJbrowser = fetcherJbrowser;
}

@JsonProperty("fetcher.chrome")
public FetcherChrome getFetcherChrome() {
return fetcherChrome;
}

@JsonProperty("fetcher.chrome")
public void setFetcherChrome(FetcherChrome fetcherChrome) {
this.fetcherChrome = fetcherChrome;
}

@JsonProperty("url.injector")
public UrlInjector getUrlInjector() {
return urlInjector;
}

@JsonProperty("url.injector")
public void setUrlInjector(UrlInjector urlInjector) {
this.urlInjector = urlInjector;
}

@JsonProperty("memex.webpage.mimetype")
public String getMemexWebpageMimetype() {
return memexWebpageMimetype;
}

@JsonProperty("memex.webpage.mimetype")
public void setMemexWebpageMimetype(String memexWebpageMimetype) {
this.memexWebpageMimetype = memexWebpageMimetype;
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