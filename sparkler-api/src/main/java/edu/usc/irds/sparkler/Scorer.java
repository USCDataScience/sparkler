package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;

import java.util.Iterator;

/**
 * Created by gtotaro on 5/15/17.
 */
public interface Scorer extends ExtensionPoint {

    Double score(String extractedText) throws Exception;

    String getScoreKey();
}
