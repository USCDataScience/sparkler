package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;

import java.util.Iterator;

/**
 * A contract for any {@link ExtensionPoint} that offers functionality to
 * determine a score for urls.
 */
public interface Scorer extends ExtensionPoint {

    Double score(String extractedText) throws Exception;

    String getScoreKey();
}