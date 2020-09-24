package edu.usc.irds.sparkler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tg on 12/10/17.
 */
public class MultiMap<K, V> extends HashMap<K, List<V>> {

    public void add(K key, V value){
        if (!this.containsKey(key)){
            this.put(key, new ArrayList<>());
        }
        this.get(key).add(value);

    }
}
