package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.Config
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.service.PluginService
import edu.usc.irds.sparkler.model._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util
import edu.usc.irds.sparkler.UrlInjectorObj


object UrlInjectorFunction extends ((SparklerJob, util.Collection[String]) => util.Collection[UrlInjectorObj]) with Serializable with Loggable{

 override def apply(job: SparklerJob, resources: util.Collection[String])
  : util.Collection[UrlInjectorObj] = {
    val fetcher:scala.Option[Config] = PluginService.getExtension(classOf[Config], job)
    try {
      fetcher match {
        case Some(fetcher) =>
          val score = fetcher.processConfig(resources)
          score
        case None =>
          convertToUrlInjector(resources)
      }
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        convertToUrlInjector(resources)
    }
  }

  def convertToUrlInjector( lst:util.Collection[String]) : util.Collection[UrlInjectorObj] = {
    var c = List[UrlInjectorObj]()
     lst.forEach(n => {
           val uio = new UrlInjectorObj(n, null, null)
          c = uio :: c
    })
    c
  }
}
