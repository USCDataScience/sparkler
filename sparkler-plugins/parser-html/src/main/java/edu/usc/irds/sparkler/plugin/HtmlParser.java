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

package edu.usc.irds.sparkler.plugin;

import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.MetadataParser;

@Extension
public class HtmlParser extends AbstractExtensionPoint implements MetadataParser {

 

    public Metadata parseMetadata(InputStream content, Metadata meta) throws Exception {

        String url = meta.get("resourceName");
        String charsetName = meta.get("Content-Encoding");
        Document doc = Jsoup.parse(content, charsetName, url);
        String body = doc.body().html();

        meta.add("body", body);
        meta.add("html", doc.outerHtml());

        return meta;
    }
}
