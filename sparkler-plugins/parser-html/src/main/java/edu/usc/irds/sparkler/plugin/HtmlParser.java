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
