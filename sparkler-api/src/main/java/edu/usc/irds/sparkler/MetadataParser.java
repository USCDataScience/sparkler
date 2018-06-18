/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
