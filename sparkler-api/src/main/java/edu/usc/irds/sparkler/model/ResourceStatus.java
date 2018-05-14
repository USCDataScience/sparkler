package edu.usc.irds.sparkler.model;

import java.io.Serializable;

/**
 * Created by karanjeetsingh on 10/22/16.
 */
public enum ResourceStatus implements Serializable {
    UNFETCHED, FETCHED, FETCHING, ERROR, IGNORED;
}
