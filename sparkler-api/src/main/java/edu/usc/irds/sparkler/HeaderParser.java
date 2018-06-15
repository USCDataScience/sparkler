package edu.usc.irds.sparkler;

//import scala.collection.immutable.Map;
import java.util.List;
import java.util.Map;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author michelad
 */
public interface HeaderParser extends ExtensionPoint{
    Map<String, Object> parseHeader(Map<String, List<String>> headers) throws Exception;
}
