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

package edu.usc.irds.sparkler;

/**
 * A contract for any {@link ExtensionPoint} that offers functionality to filter urls
 *
 * @since Sparkler 0.1
 *
 */
public interface URLFilter extends ExtensionPoint {

    /**
     * Filter Urls
     * @param url the url on which decision to be made
     * @param parent the parent or the linking url
     * @return {@code true} if the {@code url} to be passed through the filter,
     * {@code false} if the url shall be dropped
     */
    boolean filter(String url, String parent);
}
