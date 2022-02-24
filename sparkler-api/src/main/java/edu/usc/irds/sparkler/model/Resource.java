package edu.usc.irds.sparkler.model;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.util.StringUtil;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;
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
    @Field("contenthash")private String contenthash;

    private String version = "1.0";
    private Date modifiedTime = new Date();
    private String crawler = "sparkler";
    private Integer fetchDepth = 0;
    private Double pageScore = 0.0;
    private Integer retriesSinceFetch = -1;
    private Integer fetchStatusCode = 0;
    private Long responseTime = 0L;

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

    public Resource(Map<String, Object> dataMap) {
        System.out.println("Resource constructor ---------------");
        for (String key : dataMap.keySet()) {
            System.out.println(key + " => " + dataMap.get(key));
        }

        if (dataMap.containsKey("id")) id = (String)dataMap.get("id");
        if (dataMap.containsKey("url")) url = (String)dataMap.get("url");
        if (dataMap.containsKey("group")) group = (String)dataMap.get("group");
        if (dataMap.containsKey("discover_depth")) {
            try {
                discoverDepth = (Integer)dataMap.get("discover_depth");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Integer: discover_depth");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("status")) status = (String)dataMap.get("status");
        if (dataMap.containsKey("fetch_timestamp")) {
            try {
                fetchTimestamp = new SimpleDateFormat(Constants.defaultDateFormat).parse((String)dataMap.get("fetch_timestamp"));
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Date: fetch_timestamp");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("crawl_id")) crawlId = (String)dataMap.get("crawl_id");
        if (dataMap.containsKey("dedupe_id")) dedupeId = (String)dataMap.get("dedupe_id");
        if (dataMap.containsKey("*_score")) {
            try {
                score = (HashMap<String, Double>)dataMap.get("*_score");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to HashMap<String, Double>: *_score");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("generate_score")) {
            try {
                generateScore = (Double)dataMap.get("generate_score");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Double: generate_score");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("http_method")) httpMethod = (String)dataMap.get("http_method");
        if (dataMap.containsKey("jobmeta")) metadata = (String)dataMap.get("jobmeta");
        if (dataMap.containsKey("last_updated_at")) {
            try {
                lastUpdatedAt = new SimpleDateFormat(Constants.defaultDateFormat).parse((String)dataMap.get("last_updated_at"));
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Date: last_updated_at");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("indexed_at")) {
            try {
                indexedAt = new SimpleDateFormat(Constants.defaultDateFormat).parse((String)dataMap.get("indexed_at"));
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Date: indexed_at");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("hostname")) hostname = (String)dataMap.get("hostname");
        if (dataMap.containsKey("parent")) parent = (String)dataMap.get("parent");
        if (dataMap.containsKey("version")) version = (String)dataMap.get("version");
        if (dataMap.containsKey("modified_time")) {
            try {
                modifiedTime = new SimpleDateFormat(Constants.defaultDateFormat).parse((String)dataMap.get("modified_time"));
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Date: modified_time");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("crawler")) crawler = (String)dataMap.get("crawler");
        if (dataMap.containsKey("fetch_depth")) {
            try {
                fetchDepth = (Integer)dataMap.get("fetch_depth");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Integer: fetch_depth");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("page_score")) {
            try {
                pageScore = (Double)dataMap.get("page_score");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Double: page_score");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("retries_since_fetch")) {
            try {
                retriesSinceFetch = (Integer)dataMap.get("retries_since_fetch");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Integer: retries_since_fetch");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("fetch_status_code")) {
            try {
                fetchStatusCode = (Integer)dataMap.get("fetch_status_code");
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Integer: fetch_status_code");
                System.err.println(e.toString());
            }
        }
        if (dataMap.containsKey("response_time")) {
            try {
                responseTime = new Long((String)dataMap.get("response_time"));
            } catch (Exception e) {
                System.err.println("Could not retrieve and parse to Long: response_time");
                System.err.println(e.toString());
            }
        }
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

    public void setContentHash(String md5hash) {
        this.contenthash = md5hash;
    }

    public String getContentHash(){
        return this.contenthash;
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

    public String getVersion() { return this.version; }

    public Date getModifiedTime() { return this.modifiedTime; }

    public String getCrawler() { return this.crawler; }

    public Integer getFetchDepth() { return this.fetchDepth; }

    public Double getPageScore() { return this.pageScore; }

    public Integer getRetriesSinceFetch() { return this.retriesSinceFetch; }

    public Integer getFetchStatusCode() { return this.fetchStatusCode; }

    public Long getResponseTime() { return this.responseTime; }

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
            put("generate_score", getGenerateScore());
            put("http_method", getHttpMethod());
            put("jobmeta", getMetadata());
            put("last_updated_at", getLastUpdatedAt());
            put("indexed_at", getIndexedAt());
            put("hostname", getHostname());
            put("parent", getParent());
            put("version", getVersion());
            put("modified_time", getModifiedTime());
            put("crawler", getCrawler());
            put("fetch_depth", getFetchDepth());
            put("page_score", getPageScore());
            put("retries_since_fetch", getRetriesSinceFetch());
            put("fetch_status_code", getFetchStatusCode());
            put("response_time", getResponseTime());
        }};

        Map<String, Double> scores = getScore();
        for (String key : scores.keySet()) {
            dataMap.put(key, scores.get(key));
        }
        return dataMap;
    }
}
