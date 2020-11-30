package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.Config
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.service.PluginService
import edu.usc.irds.sparkler.model._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util

object UrlInjectorFunction extends ((SparklerJob, util.Collection[String]) => util.Collection[String]) with Serializable with Loggable{

 override def apply(job: SparklerJob, resources: util.Collection[String])
  : util.Collection[String] = {
    val fetcher:scala.Option[Config] = PluginService.getExtension(classOf[Config], job)
    val a = new ArrayList[String]
    try {
      fetcher match {
        case Some(fetcher) =>
          val score = fetcher.processConfig()
          score
        case None =>
          resources
      }
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        resources
    }
  }
}
