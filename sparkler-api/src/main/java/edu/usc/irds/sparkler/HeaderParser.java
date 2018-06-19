package edu.usc.irds.sparkler;

import java.util.List;
import java.util.Map;



/**
 *
 * @author michelad
 */
public interface HeaderParser extends ExtensionPoint{
    Map<String, Object> parseHeader(Map<String, List<String>> headers) throws Exception;
}
