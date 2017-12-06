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

package edu.usc.irds.sparkler;

import org.pf4j.Plugin;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract implementation of extension point
 */
public abstract class AbstractExtensionPoint extends Plugin implements ExtensionPoint  {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractExtensionPoint.class);

    protected JobContext jobContext;
    protected String pluginId;

    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper
     */
    public AbstractExtensionPoint(PluginWrapper wrapper) {
        super(wrapper);
        PluginDescriptor desciptor = getWrapper().getDescriptor();
        pluginId = String.join(":", desciptor.getProvider(), desciptor.getPluginId(), desciptor.getVersion());
    }

    /**
     * Gets Plugin ID
     * @return plugin unique identifier that includes provider:id:version
     */
    public String getPluginId(){
        return pluginId;
    }

    @Override
    public void start() throws PluginException {
        super.start();
        LOG.info("Started plugin {} from ", getWrapper().getPluginId(), getWrapper().getPluginPath());
    }

    @Override
    public void stop() throws PluginException {
        if (this instanceof AutoCloseable){
            try {
                ((AutoCloseable) this).close();
            } catch (Exception e) {
                throw new PluginException(e);
            }
        }
        LOG.info("Stopped Plugin {}", getWrapper().getPluginId());
        super.stop();
    }

    @Override
    public void init(JobContext context) throws SparklerException {
        this.jobContext = context;
        LOG.debug("Initialize the context");
    }

    /**
     * Gets a resource
     *
     * @param resourceName resource name
     * @return stream of resource or null
     * @throws IOException when an io error occurs
     */
    public InputStream getResourceAsStream(String resourceName) throws IOException {
        return getResourceAsStream(this, resourceName);
    }

    /**
     * Gets a resource
     *
     * @param extension    extension Instance
     * @param resourceName name of resource (file name)
     * @return stream of resource or null
     * @throws IOException
     */
    public static InputStream getResourceAsStream(ExtensionPoint extension, String resourceName) throws IOException {
        //TODO: allow user to specify configs outside the jar
        //this one just gets configs from the classloader
        return extension.getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}