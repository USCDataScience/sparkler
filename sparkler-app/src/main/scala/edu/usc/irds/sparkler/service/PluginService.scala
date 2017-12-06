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

package edu.usc.irds.sparkler.service

import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.SparklerJob
import org.pf4j.{DefaultPluginManager, ExtensionPoint => _}

import scala.collection.mutable

/**
  * A service for loading plugins/extensions
  *
  * @since Sparkler 0.1
  */
class PluginService(job:SparklerJob) {

  val pluginManager = new DefaultPluginManager()

  // This map keeps cache of all active instances
  val registry = new mutable.HashMap[Class[_ <: ExtensionPoint], ExtensionPoint]


  def load(): Unit ={
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        pluginManager.stopPlugins()
      }
    })
    pluginManager.loadPlugins()
    pluginManager.startPlugins()
  }


  /**
    * Checks if class is a sparkler extension
    *
    * @param clazz the class
    * @return true if the given class is an extension
    */
  def isExtensionPoint(clazz:Class[_]): Boolean = classOf[ExtensionPoint].isAssignableFrom(clazz)


  /**
    * gets list of active extensions for a given point
    *
    * @param point extension point
    * @return extension
    */
  def getExtension[X <: ExtensionPoint](point:Class[X]):Option[X] = {
    if (registry.contains(point)) {
      Some(registry(point).asInstanceOf[X])
    } else {
      val extensions = pluginManager.getExtensions(point)
      if (extensions.isEmpty) {
        None
      } else {
        if (extensions.size() > 1){
          throw new Exception("Multiple extensions enabled, but that feature isn't implemented yet: " + extensions)
        } else {
          val instance = extensions.get(0)
          registry.put(point, instance)
          Some(instance)
        }
      }
    }
  }
}

object PluginService extends Loggable{

  //TODO: weak hash map + bounded size + with a sensible expiration policy like LRU
  //TODO: plugins mapped to jobId. consider the case of having SparklerJob instance
  val cache = new mutable.HashMap[String, PluginService]()

  //val systemBundles = Set("org.apache.felix.framework")

  def getExtension[X <: ExtensionPoint](point:Class[X], job: SparklerJob):Option[X] = {
    //lazy initialization for distributed mode (wherever this code gets executed)
    if (!cache.contains(job.id)){
      this.synchronized {
        if(!cache.contains(job.id)) {
          val service = new PluginService(job)
          service.load()
          cache.put(job.id, service)
        }
      }
    }
    cache(job.id).getExtension(point)
  }

  def main(args: Array[String]): Unit = {
    System.setProperty("pf4j.mode", "development")
    System.setProperty("pf4j.pluginsDir", "plugins")
    println("Start.. " + System.getenv("PWD"))
    val job = new SparklerJob("testid", Constants.defaults.newDefaultConfig())
    println(getExtension(classOf[Fetcher], job))
    println(getExtension(classOf[URLFilter], job))
  }
}



