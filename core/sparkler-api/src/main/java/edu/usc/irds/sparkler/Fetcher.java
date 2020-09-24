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

import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;

import java.util.Iterator;

/**
 * This class defines contact for Fetcher plugins.
 * A fetcher takes a stream of uri to resources and fetches returns a
 * lazy contract for stream of fetched data.
 */
public interface Fetcher extends ExtensionPoint {

    /**
     *
     * @param resources iterator of resources to be fetched
     * @return iterator of fetched data off input resources
     * @throws Exception when an error occurs
     */
    Iterator<FetchedData> fetch(Iterator<Resource> resources) throws Exception;

}
