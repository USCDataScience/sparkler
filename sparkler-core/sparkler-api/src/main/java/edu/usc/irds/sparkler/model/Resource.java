package edu.usc.irds.sparkler.model;

import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.util.StringUtil;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by karanjeetsingh on 10/22/16.
 */
public class Resource implements Serializable {

    //NOTE: Keep the variable names in sync with solr schema
    @Field private String id;
    @Field("crawl_id") private String crawlId;
    @Field private String url;
    @Field private String group;
    @Field("fetch_timestamp") private Date fetchTimestamp;
    //@Field private Integer numTries = 0;
    //@Field private Integer numFetches = 0;
    @Field("discover_depth") private Integer discoverDepth = 0;
    //@Field("page_score") private Double score = 0.0;
    @Field("generate_score") private Double generateScore = 0.0;
    @Field("*_score") private Map<String, Double> score = new HashMap<>();
    @Field private String status = ResourceStatus.UNFETCHED.toString();
    @Field("last_updated_at") private Date lastUpdatedAt;
    @Field("indexed_at") private Date indexedAt;
    @Field private String hostname;
    @Field private String parent;
    @Field("dedupe_id") private String dedupeId;
    @Field("http_method") private String httpMethod;
    @Field("jobmeta") private String metadata;

    public Resource() {
    }

    public Resource(String url, String group, JobContext job) {
        super();
        //this.id = resourceId(url, job);
        this.url = url;
        this.group = group;
        this.hostname = group;
        this.crawlId = job.getId();
        this.dedupeId = StringUtil.sha256hash(url + "-" + job.getId());
    }

    public Resource(String url, String group, JobContext sparklerJob, Date fetchTimestamp) {
        this(url, group, sparklerJob);
        this.id = resourceId(url, sparklerJob, fetchTimestamp);
        this.fetchTimestamp = fetchTimestamp;
    }

    public Resource(String url, Integer discoverDepth, JobContext sparklerJob, ResourceStatus status) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.indexedAt = new Date();
        this.id = resourceId(url, sparklerJob, this.indexedAt);
        this.discoverDepth = discoverDepth;
        this.status = status.toString();
    }

    public Resource(String url, Integer discoverDepth, JobContext sparklerJob, ResourceStatus status,
        String parent, Map<String, Double> score, String metadata, String httpMethod) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.indexedAt = new Date();
        this.id = resourceId(url, sparklerJob, this.indexedAt);
        this.discoverDepth = discoverDepth;
        this.status = status.toString();
        this.parent = parent;
        this.score = score;
        this.httpMethod = httpMethod;
        this.metadata = metadata;
    }

    public Resource(String url, Integer discoverDepth, JobContext sparklerJob, ResourceStatus status,
                    Date fetchTimestamp, String parent) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.id = resourceId(url, sparklerJob, fetchTimestamp);
        this.discoverDepth = discoverDepth;
        this.status = status.toString();
        this.parent = parent;
    }

    public Resource(String url, String group, JobContext sparklerJob, Date fetchTimestamp, Integer numTries,
                    Integer numFetches, ResourceStatus status) {
        this(url, group, sparklerJob, fetchTimestamp);
        //this.numFetches = numFetches;
        this.status = status.toString();
    }

    public Resource(String url, Integer discoverDepth, JobContext sparklerJob, ResourceStatus status, Date fetchTimestamp, String parent, Map<String, Double> score) throws MalformedURLException {

        this(url, discoverDepth, sparklerJob, status, fetchTimestamp, parent);
        this.score = score;
        
    }

    @Override
    public String toString() {
        return String.format("Resource(%s, %s, %s, %d, %f, %s)",
                id, group, fetchTimestamp, discoverDepth, score, status);
                //id, group, fetchTimestamp, numTries, numFetches, discoverDepth, score, status);
    }

    public static String resourceId(String url, JobContext job) {
        return String.format("%s-%s", job.getId(), url);
    }

    public static String resourceId(String url, JobContext job, Date timestamp) {
        Random rand = new Random();
        int int_random = rand.nextInt(10000000);

        return StringUtil.sha256hash(String.format("%s-%s-%s-%s", job.getId(), url, timestamp.getTime(), int_random));
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGroup() {
        return group;
    }

    public Integer getDiscoverDepth() {
        return discoverDepth;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFetchTimestamp() { return fetchTimestamp; }

    public void setFetchTimestamp(Date fetchTimestamp) { this.fetchTimestamp = fetchTimestamp; }

    public String getCrawlId() { return crawlId; }

    public void setCrawlId(String crawlId) { this.crawlId = crawlId; }

    public String getDedupeId() { return dedupeId; }

    public void setDedupeId(String dedupeId) { this.dedupeId = dedupeId; }

    public Map<String, Double> getScore() {
        return this.score;
    }

    public void setScore(Map<String, Double> score) {
        this.score = score;
    }

    public Double getScore(String scoreKey) {
        return this.score.get(scoreKey);
    }

    public void setScore(String scoreKey, Double value) {
        this.score.put(scoreKey, value);
    }

    public Double getGenerateScore() {
        return this.generateScore;
    }

    public void setGenerateScore(Double generateScore) {
        this.generateScore = generateScore;
    }

    public Map<String, Double> getScoreAsMap(){
        HashMap<String, Double> hm = new HashMap<>();
        hm.put("generate_score", this.generateScore);
        return hm;
    }

    public String getHttpMethod(){
        if(this.httpMethod == null || this.httpMethod.equals("")){
            return "GET";
        } else{
            return this.httpMethod;
        }
    }

    public String getMetadata(){
        return this.metadata;
    }

    public Date getLastUpdatedAt() {
        return this.lastUpdatedAt;
    }

    public Date getIndexedAt() {
        return this.indexedAt;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getParent() {
        return this.parent;
    }

    public Map<String, Object> getDataAsMap() {
        Map<String, Object> dataMap = new HashMap<String, Object>() {{
            put("id", getId());
            put("url", getUrl());
            put("group", getGroup());
            put("discover_depth", getDiscoverDepth());
            put("status", getStatus());
            put("fetch_timestamp", getFetchTimestamp());
            put("crawl_id", getCrawlId());
            put("dedupe_id", getDedupeId());
            put("*_score", getScore());
            put("generate_score", getGenerateScore());
            put("http_method", getHttpMethod());
            put("jobmeta", getMetadata());
            put("last_updated_at", getLastUpdatedAt());
            put("indexed_at", getIndexedAt());
            put("hostname", getHostname());
            put("parent", getParent());
        }};
        return dataMap;
    }
}
