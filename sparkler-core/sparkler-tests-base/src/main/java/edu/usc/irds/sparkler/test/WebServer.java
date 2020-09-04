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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 12/28/16
 */
public class WebServer extends Server {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
    public static final int DEFAULT_PORT = 8080;

    private int port;

    public static String getDefaultPath(){
        return WebServer.class.getClassLoader().getResource("webapp").toExternalForm();
    }

    public WebServer(){
        this(DEFAULT_PORT, getDefaultPath());
    }

    public WebServer(int port, String resRoot){
        super(port);
        this.port = port;
        LOG.info("Port:{}, Resources Root:{}", port, resRoot);
        ResourceHandler rh0 = new ResourceHandler();
        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/res/*");
        context0.setResourceBase(resRoot);
        context0.setHandler(rh0);

        //ServletHandler context1 = new ServletHandler();
        //this.setHandler(context1);

        ServletContextHandler context1 =  new ServletContextHandler();
        context1.addServlet(TestSlaveServlet.class, "/slavesite/*");

        // Create a ContextHandlerCollection and set the context handlers to it.
        // This will let jetty process urls against the declared contexts in
        // order to match up content.
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context1, context0});

        this.setHandler(contexts);
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) throws Exception {
        WebServer server = new WebServer();
        server.start();
        server.join();
    }
}
