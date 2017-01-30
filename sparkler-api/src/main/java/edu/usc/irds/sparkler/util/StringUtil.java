/**
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

package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.SparklerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ksingh on 1/29/17.
 *
 * Utility class for String processing
 */
public class StringUtil {

    public static Logger LOG = LoggerFactory.getLogger(StringUtil.class);

    /**
     * Computes SHA-256 hash of a string
     * @param str
     * @return hash
     */
    public static String sha256hash(String str) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes("UTF-8"));
            byte[] digest = md.digest();
            hash = String.format("%064x", new java.math.BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Not a valid Hash Algorithm for string: " + str, e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Not a valid Encoding for string: " + str, e);
        }
        return hash;
    }

}
