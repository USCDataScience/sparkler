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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class creates a wrapper for list when it is directly specified or
 * indirectly specified as file path.
 * <p>
 * Example: File backed indirect list
 * <p>
 * userAgents: user-agents.txt
 * <p>
 * Example: Direct list
 * userAgents:
 * - UA 1
 * - UA 2
 */
public class FileBackedList extends ArrayList<String> {


    public FileBackedList() {
    }

    public FileBackedList(String contentFile) {
        super(readFile(contentFile, FileBackedList.class.getClassLoader()));
    }

    public FileBackedList(Collection<String> contents) {
        super(contents);
    }

    public static List<String> readFile(String contentFile, ClassLoader loader) {

        try (InputStream stream = loader.getResourceAsStream(contentFile)) {
            assert stream != null;
            return IOUtils.readLines(stream, Charset.defaultCharset()).stream()
                    .map(String::trim)
                    .filter(l -> !(l.startsWith("#") || l.isEmpty()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
