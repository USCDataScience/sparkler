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
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @since 12/28/16
 */
public class WebServer extends Server {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    public static final int DEFAULT_PORT = 8080;
    public static File getDefaultPath(){
        LOG.info("Looking for resources, PWD={}", new File(".").getAbsolutePath());
        String resRootPath = "src" + File.separator + "main" + File.separator + "webapp";
        File resRoot = new File(resRootPath);

        if (!resRoot.exists()) { //see if a child directory contains this
            LOG.warn("{} doesn't exists", resRoot.getAbsolutePath());
            resRoot = new File(".." + File.separator + "sparkler-tests-base" + File.separator + resRootPath);
            if (!resRoot.exists()){
                LOG.error("{} doesn't exists", resRoot.getAbsolutePath());
                throw new RuntimeException("Web resources doesn't exists");
            }
        }
        return resRoot;
    }
    public WebServer(){
        this(DEFAULT_PORT, getDefaultPath());
    }

    public WebServer(int port, File resRoot){
        super(port);

        ResourceHandler rh0 = new ResourceHandler();
        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/");
        context0.setBaseResource(Resource.newResource(resRoot));
        context0.setHandler(rh0);

        // Create a ContextHandlerCollection and set the context handlers to it.
        // This will let jetty process urls against the declared contexts in
        // order to match up content.
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context0 });

        this.setHandler(contexts);
    }

    public static void main(String[] args) throws Exception {
        WebServer server = new WebServer();
        server.start();
        server.join();
    }
}
