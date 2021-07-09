package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.{GenericProcess, Scorer}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{CrawlData, SparklerJob}
import edu.usc.irds.sparkler.service.PluginService

import java.io.Serializable

object GenericFunction
  extends ((SparklerJob, GenericProcess.Event) => GenericProcess.Event) with Serializable with Loggable {

    override def apply(job: SparklerJob, event: GenericProcess.Event) : GenericProcess.Event = {
      val genericProc:scala.Option[GenericProcess] = PluginService.getExtension(classOf[GenericProcess], job)
      try {
        genericProc match {
          case Some(genericProc) =>
            genericProc.executeProcess(event)
            LOG.info(s"Executing Event Process $event")
            event
          case None =>
            LOG.info("Event processing is not performed")
            event
        }
      } catch {
        case e: Exception =>
          LOG.error(e.getMessage, e)
          event
      }
    }
}
