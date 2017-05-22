/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.irds.sparkler.model;

import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.util.StringUtil;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Resource implements Serializable {

    //NOTE: Keep the variable names in sync with solr schema
    @Field
    private String id;
    @Field("crawl_id")
    private String crawlId;
    @Field
    private String url;
    @Field
    private String group;
    @Field("fetch_timestamp")
    private Date fetchTimestamp;
    //@Field private Integer numTries = 0;
    //@Field private Integer numFetches = 0;
    @Field("discover_depth")
    private Integer discoverDepth = 0;
    @Field
    private Double score = 0.0;
    @Field
    private String status = ResourceStatus.UNFETCHED.toString();
    @Field("last_updated_at")
    private Date lastUpdatedAt;
    @Field("indexed_at")
    private Date indexedAt;
    @Field
    private String hostname;
    @Field
    private String parent;
    @Field("dedupe_id")
    private String dedupeId;


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

    public Integer getDiscoverDepth() {
        return discoverDepth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFetchTimestamp() {
        return fetchTimestamp;
    }

    public void setFetchTimestamp(Date fetchTimestamp) {
        this.fetchTimestamp = fetchTimestamp;
    }

    public String getCrawlId() {
        return crawlId;
    }

    public void setCrawlId(String crawlId) {
        this.crawlId = crawlId;
    }

    public String getDedupeId() {
        return dedupeId;
    }

    public void setDedupeId(String dedupeId) {
        this.dedupeId = dedupeId;
    }
}
