package edu.usc.irds.sparkler;



import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
/**
 *
 * @author michelad
 */


public interface MetadataParser extends ExtensionPoint {
    Metadata parseMetadata(InputStream content, Metadata meta) throws Exception;
}
