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
package edu.usc.irds.sparkler.config;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class FetcherProps {

    @Min(value = 1, message = "fetcher.serverDelay cannot be less than 1")
    @NotNull(message = "fetcher.serverDelay cannot be null")
    private int serverDelay;

    @Valid
    @NotNull(message = "fetcher.headers cannot be null")
    private Map<String, String> headers;

    @Valid
    private FileBackedList userAgents;

    public int getServerDelay() {
        return serverDelay;
    }

    public void setServerDelay(int serverDelay) {
        this.serverDelay = serverDelay;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public FileBackedList getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(FileBackedList userAgents) {
        this.userAgents = userAgents;
    }
}
