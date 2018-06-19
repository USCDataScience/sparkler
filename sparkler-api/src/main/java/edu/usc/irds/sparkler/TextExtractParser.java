package edu.usc.irds.sparkler;

import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
/**
 *
 * @author michelad
 */
public interface TextExtractParser extends ExtensionPoint{
    String parseText(InputStream stream, Metadata meta) throws Exception;
}
