package edu.usc.irds.sparkler.pipeline

import edu.usc.irds.sparkler.{GenericProcess, Scorer}
import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{CrawlData, SparklerJob}
import edu.usc.irds.sparkler.service.PluginService
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import java.io.Serializable

object GenericFunction
  extends ((SparklerJob, GenericProcess.Event, SparkSession, RDD[CrawlData]) => GenericProcess.Event) with Serializable with Loggable {

    override def apply(job: SparklerJob, event: GenericProcess.Event, spark: SparkSession, rdds: RDD[CrawlData]) : GenericProcess.Event = {
      val genericProc:scala.Option[GenericProcess] = PluginService.getExtension(classOf[GenericProcess], job)
      try {
        genericProc match {
          case Some(genericProc) =>
            genericProc.executeProcess(event, spark)
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
