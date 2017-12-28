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


import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FetchedData implements Serializable {

    private Resource resource;

    private byte[] content;
    private String contentType;
    private Integer contentLength;
    private Map<String, List<String>> headers = Collections.emptyMap();
    private Date fetchedAt;
    private MultiMap<String, String> metadata = new MultiMap<>();
    private int responseCode;
    private long responseTime;


    public FetchedData() {
    }

    public FetchedData(byte[] content, String contentType, int responseCode, long responseTime) {
        super();
        this.content = content;
        this.contentLength = content.length;
        this.contentType = contentType;
        this.responseCode = responseCode;
        this.fetchedAt = new Date();
        this.responseTime = responseTime;
	}
	
	public String getContentType() {
        return contentType == null ? "" : contentType;
	}
	
	public int getResponseCode() {
		return responseCode;
	}

    public Resource getResource() { return resource; }

    public byte[] getContent() {
        return content;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Date getFetchedAt() {
        return fetchedAt;
    }

    public MultiMap<String, String> getMetadata() {
        return metadata;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFetchedAt(Date fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public void setMetadata(MultiMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Long getResponseTime(){return this.responseTime;}

}
