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

import java.io.{File, InputStream}
import java.net.URL
import java.util
import java.util.Properties

import edu.usc.irds.sparkler._
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.commons.io.IOUtils
import org.apache.felix.framework.cache.BundleArchive
import org.apache.felix.main.AutoProcessor
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.osgi.framework.{Constants => _, _}
import org.osgi.framework.launch.{Framework, FrameworkFactory}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

/**
  * A service for loading plugins/extensions
  *
  * @since Sparkler 0.1
  */
class PluginService(job:SparklerJob) {

  import PluginService._

  // This map has two functions
  // 1) Keys are set of known extensions
  // 2) Values are the chaining implementation class
  val knownExtensions: Map[Class[_ <: ExtensionPoint], Class[_ <: ExtensionChain[_]]] = Map(
    classOf[URLFilter] -> classOf[URLFilters] //Add more extensions and chains here
  )

  var serviceLoader:Option[Framework] = None

  // This map keeps cache of all active instances
  val registry = new mutable.HashMap[Class[_ <: ExtensionPoint], ExtensionPoint]


  /**
    * <p>
    * Loads the configuration properties in the configuration property file
    * associated with the Felix framework installation; these properties
    * are accessible to the framework and to bundles and are intended
    * for configuration purposes.
    * </p>
    *
    * @return A <tt>Map</tt> instance or <tt>null</tt> if there was an error.
    **/
  def loadFelixConfig(): mutable.Map[String, String] = {
    val prop = new Properties()
    val stream:Option[InputStream] = Some(getClass.getClassLoader.getResourceAsStream(Constants.file.FELIX_CONFIG))
    stream match {
      case Some(stream) =>
        prop.load(stream)
        IOUtils.closeQuietly(stream)
      case None =>
        throw new SparklerException(s"Could not load Felix Configuration Properties file: "
          + s"${Constants.file.FELIX_CONFIG}")
    }
    prop.asScala
  }

  /**
    * Simple method to parse META-INF/services file for framework factory.
    * Currently, it assumes the first non-commented line is the class name
    * of the framework factory implementation.
    *
    * @return The created <tt>FrameworkFactory</tt> instance.
    * @throws Exception if any errors occur.
    **/
  def getFelixFrameworkFactory: FrameworkFactory = {
    val url:Option[URL] = Some(getClass.getClassLoader.getResource(Constants.file.FELIX_FRAMEWORK_FACTORY))
    if (url.isEmpty){
      throw new SparklerException("Error Loading the Felix Framework Factory Instance")
    }
    val reader = Source.fromURL(url.get).bufferedReader()
    try {
      val lines = Source.fromURL(url.get).getLines()
        .map(_.trim).filter(_.charAt(0) != '#')
        .toArray
      if (lines.isEmpty) {
        throw new SparklerException("Felix factory settings are empty")
      }
      val factoryClass = getClass.getClassLoader.loadClass(lines(0))
      val factory:FrameworkFactory = factoryClass.newInstance().asInstanceOf[FrameworkFactory]
      factory
    } finally {
      IOUtils.closeQuietly(reader)
    }
  }

  def shutdownCleanup: Runnable = new Runnable {
    override def run(): Unit = {
      try {
        LOG.info("Going to stop Services...")
        serviceLoader match {
          case Some(loader) =>
            loader.stop()
            loader.waitForStop(0)
          case None =>
            LOG.debug("Service loader not found while trying to clean up felix")
        }
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          throw new SparklerException("Error Stopping the Felix Framework")
      }
    }
  }

  def load(): Unit ={

    // Load Felix Configuration Properties
    val felixConfig:mutable.Map[String, String] = loadFelixConfig()
    LOG.info("Felix Configuration loaded successfully")
    // Setting configuration to Auto Deploy Felix Bundles
    felixConfig(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY) = job.getConfiguration
      .get(Constants.key.PLUGINS_BUNDLE_DIRECTORY).asInstanceOf[String]

    // Register a Shutdown Hook with JVM to make sure Felix framework is cleanly
    // shutdown when JVM exits.
    Runtime.getRuntime.addShutdownHook(new Thread(shutdownCleanup, s"Felix-${job.id}"))

    // Creating an instance of the Apache Felix framework
    val felixFactory:FrameworkFactory = getFelixFrameworkFactory
    val loader = felixFactory.newFramework(felixConfig.asJava)
    serviceLoader = Some(loader)

    // Initialize the framework but don't start it yet
    loader.init()

    // Use the system bundle context to process the auto-deploy
    // and auto-install/auto-start properties.
    //AutoProcessor.process(felixConfig.asJava, loader.getBundleContext)
    BundleLoader.load(loader)

    // Start the Felix Framework
    loader.start()
  }

  object BundleLoader {

    def load(loader: Framework): Unit = {
      val bundlesDir = job.getConfiguration
        .get(Constants.key.PLUGINS_BUNDLE_DIRECTORY).asInstanceOf[String]
      val requiredBundles = job.getConfiguration
        .get(Constants.key.ACTIVE_PLUGINS).asInstanceOf[java.util.List[String]].toSet[String]

      LOG.info(s"Activated User bundles count = ${requiredBundles.size}")
      val seenBundles = mutable.HashSet[String]() // these are the plugins seen by loader

      val ctx = loader.getBundleContext
      val bundleFiles = new File(bundlesDir).listFiles().filter(_.getName.endsWith(".jar"))
      val bundles = new mutable.ListBuffer[Bundle]
      bundleFiles.foreach(bf => {
        val b = ctx.installBundle(bf.getAbsoluteFile.toURI.toString)
        val name = b.getSymbolicName
        if (requiredBundles.contains(name)){
          LOG.info(s"Bundle Available ${name}")
          seenBundles += name
          bundles += b
          LOG.info("Starting the bundle name...")
          b.start()
        } else {
          LOG.info(s"Bundle available but not required : ${name}.")
          b.uninstall()
        }
      })
      if (requiredBundles.size != seenBundles.size){
        val diff = requiredBundles -- seenBundles
        LOG.error(s"Missing : $diff")
        throw new SparklerException(s"Plugin bundles are missing $diff." +
          s" Either remove them from `plugins.active` or make them available to loader")
      }
    }
  }

  /**
    * Checks if class is a sparkler extension
    *
    * @param clazz the class
    * @return true if the given class is an extension
    */
  def isExtensionPoint(clazz:Class[_]): Boolean =
    clazz != null && classOf[ExtensionPoint].isAssignableFrom(clazz)

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
    if (registry.contains(point)) {
      Some(registry(point).asInstanceOf[X])
    } else {
      if (serviceLoader.isEmpty) {
        throw new Exception("Service Loader not initialized.")
      }
      val bundleCtxt: BundleContext = serviceLoader.get.getBundleContext
      LOG.debug(bundleCtxt.getBundles.mkString(" "))
      val references:Array[ServiceReference[_]] = bundleCtxt.getAllServiceReferences(point.getName, null)
      if (references != null && references.length > 0){
        val first: ServiceReference[_] = references(0)
        if (references.length > 1) {
          LOG.warn(s"More than one plugin available for $point. Selected: $first")
        }
        val instance: X = bundleCtxt.getService(first).asInstanceOf[X]
        instance.init(job, first.getBundle.getSymbolicName)
        registry.put(point, instance)
        Some(instance)
      } else {
        None
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
    println("Start")
    val job = new SparklerJob("testid", Constants.defaults.newDefaultConfig())
    val ext = getExtension(classOf[Fetcher], job)
    print(ext)
  }
}
