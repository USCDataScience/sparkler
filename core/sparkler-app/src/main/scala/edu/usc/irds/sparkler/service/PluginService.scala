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

import java.util.Collections

import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.Constants.key
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.SparklerJob
import org.pf4j.{DefaultPluginManager, PluginWrapper, ExtensionPoint => _}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
  * A service for loading plugins/extensions
  *
  * @since Sparkler 0.1
  */
class PluginService(job:SparklerJob) {
  import PluginService._

  val pluginManager = new DefaultPluginManager()

  // This map keeps cache of all active instances
  val registry = new mutable.HashMap[Class[_ <: ExtensionPoint], ExtensionPoint]
  val class2Id = new mutable.HashMap[String, String]
  val id2Class = new mutable.HashMap[String, String]

  def load(): Unit ={
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        LOG.warn("Stopping all plugins... Runtime is about to exit.")
        pluginManager.stopPlugins()
      }
    })
    LOG.info("Loading plugins...")
    pluginManager.loadPlugins()
    val config = job.getConfiguration
    val activePlugins =
      if (config.containsKey(key.ACTIVE_PLUGINS)){
        config.get(key.ACTIVE_PLUGINS).asInstanceOf[java.util.List[String]]
      } else {
        Collections.EMPTY_LIST.asInstanceOf[java.util.List[String]]
      }
    LOG.info(s"${activePlugins.size} plugin(s) Active: $activePlugins")
    val avilPlugins: mutable.Buffer[PluginWrapper] = pluginManager.getPlugins
    val unused = avilPlugins.map(p => p.getPluginId).toSet -- activePlugins.toSet
    LOG.warn(s"${unused.size} extra plugin(s) available but not activated: $unused")

    for (pluginId <- activePlugins) {
      LOG.debug(s"Loading $pluginId")
      val pw = pluginManager.getPlugin(pluginId)
      if (pw == null){ throw new Exception(s"Failed to load extension $pluginId") }
      pluginManager.startPlugin(pluginId)
      val exts = pluginManager.getExtensions(pluginId)
      for (ext <- exts){
        class2Id(ext.getClass.getName) = pw.getPluginId
        id2Class(pw.getPluginId) = ext.getClass.getName
      }
    }
    assert(class2Id.size == id2Class.size)
    LOG.info(s"Recognised Plugins: $id2Class")
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
        if (extensions.size() >  1){
          if (job.extChain.isDefinedAt(point)) { // we know how to chain these extensions
            LOG.info(s"Chaining $extensions using ${job.extChain(point)}")
            val chainedExt = job.extChain(point).newInstance().asInstanceOf[ExtensionChain[X]]
            for (instance <- extensions){
              val pluginId = class2Id.getOrElse(instance.getClass.getName, "")
              LOG.info(s"Initialize ${instance.getClass} as $pluginId")
              instance.init(job, pluginId)
            }
            chainedExt.setExtensions(extensions)
            chainedExt.init(job, "")
            registry.put(point, chainedExt)
            Some(chainedExt.asInstanceOf[X])
          } else {
            val extIds = extensions.map(x => x.getClass.getName).map(x => class2Id.getOrElse(x, x)).toList
            throw new Exception(s"Conflict between extensions: $extIds. Only one is supported in this version." +
              s" Please comment all but one in sparkler-default.yaml:plugins.active")
          }
        } else {
          val instance = extensions.get(0)
          val pluginId = class2Id.getOrElse(instance.getClass.getName, "")
          LOG.info(s"Initialize ${instance.getClass} as $pluginId")
          instance.init(job, pluginId)
          registry.put(point, instance)
          Some(instance)
        }
      }
    }
  }
}

object PluginService extends Loggable {

  //TODO: weak hash map + bounded size + with a sensible expiration policy like LRU
  //TODO: plugins mapped to jobId. consider the case of having SparklerJob instance
  val cache = new mutable.HashMap[String, PluginService]()

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
}



