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
import java.util.Date;

public class FetchedData implements Serializable {

    private Resource _resource;
    private byte[] _content;
    private String _contentType;
    private Integer _contentLength;
    private String[] _headers;
    private Date _fetchedAt;
    private int _responseCode;
    private String[] _outlinks;
    private String _html;
    private int _lastOutlinkInserted;

    public FetchedData(byte[] content, String contentType, int responseCode) {
        _content = content;
        _contentLength = content.length;
        _contentType = contentType;
        _responseCode = responseCode;
        _outlinks = new String[0];
        _lastOutlinkInserted = 0;
    }

    public String contentType() {
        return _contentType;
    }

    public int responseCode() {
        return _responseCode;
    }

    public String[] outlinks() {
        if (_outlinks == null)
            return new String[0];

        return _outlinks;
    }

    public void addOutlink(String o) {
        // Avoid Serialization error, no ArrayList.
        int l = _outlinks.length;
        if (_lastOutlinkInserted >= 0) {
            String[] prev = _outlinks;
            _outlinks = new String[l+1];
            for (int i = 0; i < prev.length; i++) {
                _outlinks[i] = prev[i];
            }
        }
        _outlinks[_lastOutlinkInserted] = o;
        _lastOutlinkInserted++;
    }

    public void addOutlinks(String[] outlinks) {
        _outlinks = outlinks;
    }

    public Resource resource() {
        return _resource;
    }

    public void setResource(Resource resource) {
        _resource = resource;
    }

    public byte[] content() {
        return _content;
    }

    public Integer contentLength() {
        return _contentLength;
    }

    public String[] headers() {
        return _headers;
    }

    public Date fetchedAt() {
        return _fetchedAt;
    }
}
