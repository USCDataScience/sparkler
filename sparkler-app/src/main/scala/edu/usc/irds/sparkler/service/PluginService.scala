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

import edu.usc.irds.sparkler.model.SparklerJob
import edu.usc.irds.sparkler.plugin.RegexURLFilter
import edu.usc.irds.sparkler.{ExtensionPoint, SparklerException, URLFilter}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks

/**
  * Created by thammegr on 6/22/16.
  */
object PluginService {

  val LOG = LoggerFactory.getLogger(PluginService.getClass)

  val knownExtensions: Set[Class[_ <: ExtensionPoint]] = Set(classOf[URLFilter])

  //TODO: make use of OSGi
  var serviceLoader = PluginService.getClass.getClassLoader

  val registry = new mutable.HashMap[Class[_ <: ExtensionPoint], mutable.ListBuffer[ExtensionPoint]]

  def load(job:SparklerJob): Unit ={

    //TODO: get this from config
    val activePlugins = Set(classOf[RegexURLFilter].getClass.getName)
    LOG.info("Found {} active plugins", activePlugins.size)
    for (pluginClassName <- activePlugins) {
      LOG.debug("Loading {}", pluginClassName)
      val extPointInstance = serviceLoader.loadClass(pluginClassName).asInstanceOf[ExtensionPoint]
      extPointInstance.init(job)
      val extPoint = detectExtensionPoint(extPointInstance)
      if (extPoint.isDefined) {
        if (!registry.contains(extPoint.get)){
          registry(extPoint.get) = new mutable.ListBuffer[ExtensionPoint]
        }
        registry(extPoint.get) += extPointInstance
        LOG.debug(s"Extension $pluginClassName (instance: ${extPointInstance.hashCode()}) " +
          s"will be bound to point ${extPoint.get.getName}")
      } else {
        throw new SparklerException(s"Could not determine ExtensionPoint for $pluginClassName." +
          s" Either the class is invalid or the extension point may not be known/active." +
          s" The active extension points are ${knownExtensions}")
      }
    }
  }

  /**
    * Detects the point where an extension can be plugged into sparkler
    *
    * @param extension an instance of the extension
    * @return A point where the extension can be plugged
    */
  def detectExtensionPoint(extension: ExtensionPoint): Option[Class[ExtensionPoint]] ={
    val buffer = mutable.Queue[Class[_]](extension.getClass)
    var result:Option[Class[ExtensionPoint]] = None
    val breakable = new Breaks
    breakable.breakable {
      for (elem <- buffer if classOf[ExtensionPoint].isAssignableFrom(elem)) {
        val extensionClass = elem.asInstanceOf[Class[ExtensionPoint]]
        if (knownExtensions.contains(extensionClass)) {
          result = Some(extensionClass)
          breakable.break()
        }
        buffer += elem.getClass.getSuperclass
        buffer ++= elem.getClass.getInterfaces
      }
    }
    result
  }

  /**
    * gets list of active extensions for a given point
    *
    * @param point extension point
    * @return list of Extensions for a given point
    */
  def getExtensions(point:Class[_ <: ExtensionPoint]):ListBuffer[ExtensionPoint] = registry(point)

}
