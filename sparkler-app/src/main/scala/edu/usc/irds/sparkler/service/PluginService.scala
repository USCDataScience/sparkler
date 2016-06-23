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
import edu.usc.irds.sparkler.{ExtensionChain, ExtensionPoint, SparklerException, URLFilter}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * A service for loading plugins/extensions
  * @since Sparkler 0.1
  */
object PluginService {

  val LOG = LoggerFactory.getLogger(PluginService.getClass)

  // This map has two functions
  // 1) Keys are set of known extensions
  // 2) Values are the chaining implementation class
  val knownExtensions: Map[Class[_ <: ExtensionPoint], Class[_ <: ExtensionChain[_]]] = Map(
    classOf[URLFilter] -> classOf[URLFilters] //Add more extensions and chains here
  )

  //TODO: get these form config


  //TODO: make use of OSGi
  var serviceLoader = PluginService.getClass.getClassLoader

  // This map keeps cache of all active instances
  val registry = new mutable.HashMap[Class[_ <: ExtensionPoint], ExtensionPoint]

  def load(job:SparklerJob): Unit ={

    //TODO: get this from config
    val activePlugins = Set(classOf[RegexURLFilter].getName)
    LOG.info("Found {} active plugins", activePlugins.size)

    // This map keeps cache of all active instances
    val buffer = new mutable.HashMap[Class[_ <: ExtensionPoint], mutable.ListBuffer[ExtensionPoint]]

    for (pluginClassName <- activePlugins) {
      LOG.debug("Loading {}", pluginClassName)
      val extPointInstance = serviceLoader.loadClass(pluginClassName).newInstance().asInstanceOf[ExtensionPoint]
      extPointInstance.init(job)

      val extPoint = detectExtensionPoint(extPointInstance)
      if (extPoint.isDefined) {
        if (!buffer.contains(extPoint.get)){
          buffer(extPoint.get) = new mutable.ListBuffer[ExtensionPoint]
        }
        buffer(extPoint.get) += extPointInstance
        LOG.debug(s"Extension $pluginClassName (instance: ${extPointInstance.hashCode()}) " +
          s"will be bound to point ${extPoint.get.getName}")
      } else {
        throw new SparklerException(s"Could not determine ExtensionPoint for $pluginClassName." +
          s" Either the class is invalid or the extension point may not be known/active." +
          s" The active extension points are ${knownExtensions.keySet.map(_.getName)}")
      }
    }

    for ((xPtClass, xChainClass) <- knownExtensions ) {

      val chain = xChainClass.newInstance().asInstanceOf[ExtensionChain[ExtensionPoint]]
      // validation
      // Minimum extensions
      val activatedPoints = buffer.getOrElse(xPtClass, List.empty)
      if (activatedPoints.size < chain.getMinimumRequired) {
        throw new SparklerException(s"Atleast ${chain.getMinimumRequired} extension(s) are required for $xPtClass" +
          s" but ${activatedPoints.size} are activated")
      }

      if (activatedPoints.size > chain.getMaximumAllowed) {
        throw new SparklerException(s"At most ${chain.getMaximumAllowed} extension(s) are allowed for $xPtClass" +
          s" but ${activatedPoints.size} are activated")
      }
      LOG.debug(s"Registering extension Chain ${xChainClass.getName}")
      chain.setExtensions(activatedPoints.asJava)
      registry.put(xPtClass, chain)
    }
  }

  /**
    * Checks if class is a sparkler extension
    *
    * @param clazz the class
    * @return true if the given class is an extension
    */
  def isExtensionPoint(clazz:Class[_]): Boolean = clazz != null && classOf[ExtensionPoint].isAssignableFrom(clazz)

  /**
    * Detects the point where an extension can be plugged into sparkler
    *
    * @param extension an instance of the extension
    * @return A point where the extension can be plugged
    */
  def detectExtensionPoint(extension: ExtensionPoint): Option[Class[_ <: ExtensionPoint]] ={
    val buffer = mutable.Queue[Class[_]](extension.getClass)
    var result:Option[Class[_ <: ExtensionPoint]] = None
    var detected = false
      while (!detected && buffer.nonEmpty){
        val clazz = buffer.dequeue()
        val extensionClass = clazz.asInstanceOf[Class[_ <: ExtensionPoint]]
        if (knownExtensions.contains(extensionClass)) {
          result = Some(extensionClass)
          detected = true
        }
        if (isExtensionPoint(clazz.getSuperclass)){
          buffer += clazz.getSuperclass
        }
        buffer ++= clazz.getInterfaces.filter(isExtensionPoint)
      }
    result
  }

  /**
    * gets list of active extensions for a given point
    *
    * @param point extension point
    * @return extension
    */
  def getExtension[X <: ExtensionPoint](point:Class[X]):Option[X] = {
    if (registry.contains(point)) Some(registry(point).asInstanceOf[X]) else None
  }
}
