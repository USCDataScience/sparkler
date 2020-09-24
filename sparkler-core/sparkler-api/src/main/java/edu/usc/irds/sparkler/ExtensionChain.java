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

import java.util.List;


/**
 * This interface defines contract for chaining and validating extension points
 * @param <X> the extension point
 * @since Sparkler 0.1
 */
public interface ExtensionChain<X extends ExtensionPoint> extends ExtensionPoint {

    /**
     * Setter for extensions
     * @param extensions list of extensions
     */
    void setExtensions(List<X> extensions);

    /**
     * Getter for extensions
     * @return List of extensions
     */
    List<X> getExtensions();

    /**
     * Gets number of extensions currently active
     * @return number of extensions currently active
     */
    default int size(){
        return getExtensions() != null ? getExtensions().size() : 0;
    }

    /**
     * Gets minimum number of points in the chain to be a valid chain.
     * For example, if atleast one extension is required return 1. If this chain is optional, return 0.
     * @return minimum number of plugins/X-points required
     */
    int getMinimumRequired();

    /**
     * Gets maximum number of extensions allowed in this chain.
     * For example. if at most one should be configured then return 1.
     *
     * @return maximum number of extensions allowed in this chain
     */
    int getMaximumAllowed();
}
