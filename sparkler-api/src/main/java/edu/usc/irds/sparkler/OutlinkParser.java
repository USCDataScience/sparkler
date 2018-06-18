/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
