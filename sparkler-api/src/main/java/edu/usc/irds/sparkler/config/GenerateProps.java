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


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GenerateProps implements BaseConfig {
    @NotNull(message = "generate.topN cannot be null")
    @Min(value = 1, message = "generate.topN value cannot be less than 1")
    private int topN;
    @NotNull(message = "generate.topGroups cannot be null")
    @Min(value = 1, message = "generate.topGroups value cannot be less than 1")
    private int topGroups;

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public int getTopGroups() {
        return topGroups;
    }

    public void setTopGroups(int topGroups) {
        this.topGroups = topGroups;
    }
}
