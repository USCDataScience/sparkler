package edu.usc.irds.sparkler;

import java.util.Iterator;

/**
 * Created by karanjeetsingh on 10/23/16.
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}
