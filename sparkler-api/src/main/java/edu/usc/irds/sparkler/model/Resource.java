package edu.usc.irds.sparkler.model;

import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.util.StringUtil;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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
    @Field("crawler_discover_depth") private Integer crawlerDiscoverDepth = 0;
    @Field private Double score = 0.0;
    @Field private String status = ResourceStatus.UNFETCHED.toString();
    @Field("last_updated_at") private Date lastUpdatedAt;
    @Field("indexed_at") private Date indexedAt;
    @Field private String hostname;

    public Resource() {
    }

    public Resource(String url, String group, JobContext job) {
        super();
        //this.id = resourceId(url, job);
        this.url = url;
        this.group = group;
        this.hostname = group;
        this.crawlId = job.getId();
    }

    public Resource(String url, String group, JobContext sparklerJob, Date fetchTimestamp) {
        this(url, group, sparklerJob);
        this.id = resourceId(url, sparklerJob, fetchTimestamp);
        this.fetchTimestamp = fetchTimestamp;
    }

    public Resource(String url, Integer crawlerDiscoverDepth, JobContext sparklerJob, ResourceStatus status) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.indexedAt = new Date();
        this.id = resourceId(url, sparklerJob, this.indexedAt);
        this.crawlerDiscoverDepth = crawlerDiscoverDepth;
        this.status = status.toString();
    }

    public Resource(String url, Integer crawlerDiscoverDepth, JobContext sparklerJob, ResourceStatus status, Date fetchTimestamp) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.id = resourceId(url, sparklerJob, fetchTimestamp);
        this.crawlerDiscoverDepth = crawlerDiscoverDepth;
        this.status = status.toString();
    }

    public Resource(String url, String group, JobContext sparklerJob, Date fetchTimestamp, Integer numTries,
                    Integer numFetches, ResourceStatus status) {
        this(url, group, sparklerJob, fetchTimestamp);
        //this.numTries = numTries;
        //this.numFetches = numFetches;
        this.status = status.toString();
    }

    @Override
    public String toString() {
        return String.format("Resource(%s, $s, %s, %s, %s, %s, %s, %s)",
                id, group, fetchTimestamp, crawlerDiscoverDepth, score, status);
                //id, group, fetchTimestamp, numTries, numFetches, crawlerDiscoverDepth, score, status);
    }


    public static String resourceId(String url, JobContext job) {
        return String.format("%s-%s", job.getId(), url);
    }

    public static String resourceId(String url, JobContext job, Date timestamp) {
        return StringUtil.sha256hash(String.format("%s-%s-%s", job.getId(), url, timestamp.getTime()));
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

    public Integer getCrawlerDiscoverDepth() {
        return crawlerDiscoverDepth;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFetchTimestamp() { return fetchTimestamp; }

    public void setFetchTimestamp(Date fetchTimestamp) { this.fetchTimestamp = fetchTimestamp; }
}
