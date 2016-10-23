package edu.usc.irds.sparkler.model;

import edu.usc.irds.sparkler.JobContext;
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
    @Field private String jobId;
    @Field private String url;
    @Field private String group;
    @Field private Date lastFetchedAt;
    @Field private Integer numTries = 0;
    @Field private Integer numFetches = 0;
    @Field private Integer depth = 0;
    @Field private Double score = 0.0;
    @Field private String status = ResourceStatus.NEW.toString();
    @Field private Date lastUpdatedAt;

    public Resource() {
    }

    public Resource(String url, String group, JobContext job) {
        super();
        this.id = resourceId(url, job);
        this.url = url;
        this.group = group;
        this.jobId = job.getId();
    }

    public Resource(String url, String group, JobContext sparklerJob, Date lastFetchedAt) {
        this(url, group, sparklerJob);
        this.lastFetchedAt = lastFetchedAt;
    }

    public Resource(String url, Integer depth, JobContext sparklerJob, ResourceStatus status) throws MalformedURLException {
        this(url, new URL(url).getHost(), sparklerJob);
        this.depth = depth;
        this.status = status.toString();
    }

    public Resource(String url, String group, JobContext sparklerJob, Date lastFetchedAt, Integer numTries,
                    Integer numFetches, ResourceStatus status) {
        this(url, group, sparklerJob, lastFetchedAt);
        this.numTries = numTries;
        this.numFetches = numFetches;
        this.status = status.toString();
    }

    @Override
    public String toString() {
        return String.format("Resource(%s, $s, %s, %s, %s, %s, %s, %s)",
                id, group, lastFetchedAt, numTries, numFetches, depth, score, status);
    }


    public static String resourceId(String url, JobContext job) {
        return String.format("%s-%s", job.getId(), url);
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

    public Integer getDepth() {
        return depth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
