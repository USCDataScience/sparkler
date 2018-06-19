package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.HeaderParser;
import java.io.InputStream;
import java.util.List;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import edu.usc.irds.sparkler.MetadataParser;
import edu.usc.irds.sparkler.OutlinkParser;
import edu.usc.irds.sparkler.TextExtractParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author michelad
 */
public class ParserDefault extends AbstractExtensionPoint implements MetadataParser, HeaderParser, OutlinkParser, TextExtractParser {

    AutoDetectParser parser;
    LinkContentHandler linkHandler;

    @Override
    public Metadata parseMetadata(InputStream stream, Metadata meta) throws Exception {
        WriteOutContentHandler outHandler = new WriteOutContentHandler();
        BodyContentHandler contentHandler = new BodyContentHandler(outHandler);
        parser.parse(stream, contentHandler, meta);
        return meta;
    }

    @Override
    public List<Link> parseOutlink(InputStream stream, String url) throws Exception {
       Metadata meta = new Metadata();
         meta.set("resourceName", url);
         
        linkHandler = new LinkContentHandler();
        parser = new AutoDetectParser();
        parser.parse(stream, linkHandler, meta);
        return linkHandler.getLinks();
    }

    @Override
    public String parseText(InputStream stream, Metadata meta) throws Exception {

        WriteOutContentHandler outHandler = new WriteOutContentHandler();
        BodyContentHandler contentHandler = new BodyContentHandler(outHandler);
        parser.parse(stream, contentHandler, meta);

        return outHandler.toString();
    }

    @Override
    public Map<String, Object> parseHeader(Map<String, List<String>> headers) throws Exception {

//    Array[] dateHeadersArrays = new Array("Date", "Last-Modified", "Expires");
        Set<String> dateHeaders = new HashSet<String>(Arrays.asList("Date", "Last-Modified", "Expires"));
        Set<String> intHeaders = new HashSet<String>(Arrays.asList("ContentLength"));

        SimpleDateFormat dateFmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

        Map<String, Object> result = new HashMap<String, Object>();
        for (String name : headers.keySet()) {
            List<String> values = headers.get(name);
            Object parsed = values;
            if (values.size() == 1) {
                Object value = values.get(0);
                parsed = value;
                try {
                    if (dateHeaders.contains(name)) {
                        parsed = parseDate(value);
                    } else if (intHeaders.contains(name)) {
                        parsed = new Long((String) value);
                    }
                } catch (Exception e) {
//                    LOG.debug(e.getMessage, e)
                } finally {
                    result.put(name, parsed);
                }
            }
        }
        return result;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     /**
   * Parse date string as per RFC7231 https://tools.ietf.org/html/rfc7231#section-7.1.1.1
   */
    Object parseDate(Object dateS) throws ParseException {
        String dateStr = (String) dateS;
        SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        Date date = httpDateFormat.parse(dateStr.trim());
        return date;
    }
}
