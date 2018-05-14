/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.usc.irds.sparkler.solr.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Created by thammegowda
 * Modified by karanjeetsingh
 */
public class StringEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(StringEvaluator.class);

    /**
     * Eval contract for evaluating string to {@code T} type object
     * @param <T> type
     */
    private interface Eval<T extends Serializable> {
        /**
         * Evaluate string to a type object
         * @param s string whose value needs to be evaluated
         * @return Type object
         */
        T eval(String s);
    }

    public static String INT_REGEX = "^[+-]?\\d{1," +
            (("" + Integer.MAX_VALUE).length() -1) +"}$";

    public static String LONG_REGEX = "^[+-]?\\d{1," +
            (("" + Long.MAX_VALUE).length() - 1) +"}$";

    public static String BOOL_REGEX = "(?i)^(true|false)$";
    public static String DOUBLE_REGEX = "^[+-]?\\d+(\\.\\d+)?([Ee][+-]\\d+)?$";
    private static LinkedHashMap<String, Eval> evals = new LinkedHashMap<>();
    static {
        //regex  -> evaluator
        //evals.put(INT_REGEX, Integer::parseInt); //Let the long take it
        evals.put(LONG_REGEX, Long::parseLong);
        evals.put(BOOL_REGEX, Boolean::parseBoolean);
        evals.put(DOUBLE_REGEX, Double::parseDouble);
    }

    /**
     * makes best effort to detect content type. Upon failure,
     * the argument string is replied back
     * @param s string whose content needs to be casted to sophisticated type
     * @return the casted object upon success or the same object when no casting is done
     */
    public Object valueOf(String s){
        s = s.trim();
        if (s.isEmpty()) {
            return s;
        }

        for (String regex: evals.keySet()) {
            if (s.matches(regex)) {
                //System.out.println(regex);
                return evals.get(regex).eval(s);
            }
        }
        return s;
    }

    /**
     * checks if this can eval object (could be string or array of strings)
     * @param object object which needs to be evaluated
     * @return true eval possible
     */
    public boolean canEval(Object object){
        return String.class.equals(object.getClass()) ||
                ( object.getClass().isArray() && object.getClass()
                        .getComponentType().equals(String.class));
    }

    /**
     * evaluates object (string or array of string)
     * @see #canEval(Object)
     * @param object the object which needs to be evaluated
     * @return object created by evaluating the argument
     * @throws IllegalArgumentException when eval not possible,
     * check {@link #canEval(Object)} prior calling this method
     */
    public Object eval(Object object){
        if (object.getClass().equals(String.class)) {
            return valueOf(object.toString());
        } else if (object.getClass().isArray() &&
                object.getClass().getComponentType().equals(String.class)){
            String[] items = (String[]) object;

            if (items.length < 1) {
                return null;
            }
            Collection result = new ArrayList<>();
            Object first = null;
            int idx = 0;
            while(idx < items.length && (first = valueOf(items[idx++])) == null);
            if (first != null) {
                result.add(first);
                for (; idx < items.length; idx++) {
                    Object target = valueOf(items[idx]);
                    if (target != null) {
                        if (target.getClass().equals(first.getClass())) {
                            result.add(target);
                        } else {
                            LOG.error("SKIPPED : Found Different types in same array {} {}", first.getClass(), target.getClass());
                        }
                    }
                }
            }
            return result;
        } else {
            //check canEval()
            throw new IllegalArgumentException("Eval not possible for " + object.getClass());
        }
    }
}
