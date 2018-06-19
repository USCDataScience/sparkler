package edu.usc.irds.sparkler;

import java.io.InputStream;
import java.util.List;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.Link;

/**
 *
 * @author michelad
 */
public interface OutlinkParser extends ExtensionPoint{
    List<Link> parseOutlink(InputStream stream, String url) throws Exception;
    
}
