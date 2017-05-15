package edu.usc.irds.sparkler;

import edu.usc.irds.sparkler.model.FetchedData;

/**
 * Created by gtotaro on 5/15/17.
 */
public interface Scorer extends ExtensionPoint {
    void score(FetchedData fetchedData) throws Exception;
}
