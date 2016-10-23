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

    //NOTE: keep the variable names in sync with solr schema and the constants below
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


    // Fields
    public static final String ID = "id";
    public static final String JOBID = "jobId";
    public static final String URL = "url";
    public static final String GROUP = "group";
    public static final String LAST_FETCHED_AT = "lastFetchedAt";
    public static final String NUM_TRIES = "numTries";
    public static final String NUM_FETCHES = "numFetches";
    public static final String DEPTH = "depth";
    public static final String SCORE = "score";
    public static final String STATUS = "status";
    public static final String LAST_UPDATED_AT = "lastUpdatedAt";
    public static final String PLAIN_TEXT = "plainText";
    public static final String MD_SUFFIX = "_md";

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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
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

    public void setGroup(String group) {
        this.group = group;
    }

    public Date getLastFetchedAt() {
        return lastFetchedAt;
    }

    public void setLastFetchedAt(Date lastFetchedAt) {
        this.lastFetchedAt = lastFetchedAt;
    }

    public Integer getNumTries() {
        return numTries;
    }

    public void setNumTries(Integer numTries) {
        this.numTries = numTries;
    }

    public Integer getNumFetches() {
        return numFetches;
    }

    public void setNumFetches(Integer numFetches) {
        this.numFetches = numFetches;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

}
