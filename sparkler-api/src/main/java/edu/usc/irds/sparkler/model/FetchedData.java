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


import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Metadata;

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
    private Metadata metadata;
    private int responseCode;


    public FetchedData() {
    }

    public FetchedData(byte[] content, String contentType, int responseCode) {
        super();
        this.content = content;
        this.contentLength = content.length;
        this.contentType = contentType;
        this.responseCode = responseCode;
        this.metadata = new Metadata();
        this.fetchedAt = new Date();
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    // TODO: Move this to Util package
    public org.apache.nutch.protocol.Content toNutchContent(Configuration conf) {
        return new org.apache.nutch.protocol.Content(resource.getUrl(), resource.getUrl(), content, contentType, metadata, conf);
    }

}
