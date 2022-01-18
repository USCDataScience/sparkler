/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.usc.irds.sparkler.test;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.io.IOException;
import java.net.Socket;

/**
 * This implementation of {@link RunListener} starts a web server before the tests
 * and gracefully stops it when tests end.
 *
 * Check port availability for tests that depend on the listener and run in parallel
 *
 * TODO: Improve this to run from Scala / SBT
 */
public class WebServerRunListener extends RunListener {

    private static final int MAX_RETRIES = 5;
    private static final int WAIT_FOR_PORT_MS = 5000;

    private WebServer server = new WebServer();

    private boolean isPortInUse(int port) {
        // Assume the port is not in use
        boolean isInUse = false;
        try {
            (new Socket("localhost", port)).close();
            isInUse = true;
        }
        catch(IOException e) {
            // Could not connect. Pass
        }
        return isInUse;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        int numRetries = 0;
        while (isPortInUse(server.getPort())) {
            if (numRetries > MAX_RETRIES) {
                throw new Exception("WebServerRunListener: Port " + server.getPort() + " is already in use!");
            }
            System.out.println("WebServerRunListener: Waiting for port: " + server.getPort());
            Thread.sleep(WAIT_FOR_PORT_MS);
            numRetries += 1;
        }
        server.start();
        System.out.println("WebServerRunListener: STARTED");
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        server.stop();
        System.out.println("WebServerRunListener: STOPPED");
    }
}
