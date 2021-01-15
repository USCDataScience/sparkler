package edu.usc.irds.sparkler.service

import edu.usc.irds.sparkler.{Constants, SparklerConfiguration}
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus}
import edu.usc.irds.sparkler.util.JobUtil
import edu.usc.irds.sparkler.model.{Resource, ResourceStatus, SparklerJob}
import edu.usc.irds.sparkler.pipeline.UrlInjectorFunction
import org.apache.commons.validator.routines.UrlValidator

import scala.collection.JavaConversions._
import java.io.File
import java.nio.file.NotDirectoryException
import java.util
import scala.collection.mutable.{ArrayBuffer, Stack}
import scala.io.Source

class InjectorRunner{
  import Injector.LOG
  val urlValidator: UrlValidator = new UrlValidator()

  val conf: SparklerConfiguration = Constants.defaults.newDefaultConfig()

  def runInjector(configOverride: Array[Any], sparkSolr: String, jobId: String, seedFile: File, seedUrls: Array[String]) : String = {
    if (configOverride != null && !configOverride.isEmpty ){
      conf.overloadConfig(configOverride.mkString(" "));
    }
    if (sparkSolr != null && !sparkSolr.isEmpty) {
      val uri = conf.asInstanceOf[java.util.HashMap[String, String]]
      (uri).put("crawldb.uri", sparkSolr)
    }

    var jobI: String = jobId
    if (jobId.isEmpty) {
      jobI = JobUtil.newJobId()
    }
    val job = new SparklerJob(jobId, conf)

    val urls: util.Collection[String] =
      if (seedFile != null) {
        if (seedFile.isFile) {
          Source.fromFile(seedFile).getLines().toList
        } else {
          stackListFiles(seedFile).par.flatMap((file) => Source.fromFile(file).getLines()).toList
        }
      } else {
        seedUrls.toList
      }

    val replacedUrls = UrlInjectorFunction(job, urls)
    // TODO: Add URL normalizer and filters before injecting the seeds
    var seeds = List[Resource]()
    replacedUrls.forEach(n => {
      if(urlValidator.isValid(n.getUrl)){
        val res = new Resource(n.getUrl.trim, 0, job, ResourceStatus.UNFETCHED, Injector.SEED_PARENT, Injector.SEED_SCORE, n.getMetadata, n.getHttpMethod)
        seeds = res :: seeds
      }
    })
    LOG.info("Injecting {} seeds", seeds.size())

    val solrClient = job.newCrawlDbSolrClient()
    solrClient.addResources(seeds.iterator())
    solrClient.commitCrawlDb()
    solrClient.close()

    jobI
  }

  def stackListFiles(directory: File): Array[File] = {
    if (!directory.isDirectory) throw new NotDirectoryException(directory.getName + " is not a directory")
    val stack = Stack[File](directory)
    val arrayBuffer = ArrayBuffer[File]()
    while (stack.nonEmpty) {
      val directory = stack.pop
      for (i <- directory.listFiles) {
        //TODO: Should this only read .txt files?
        if (i.isFile && i.getName.endsWith(".txt")) {
          arrayBuffer.append(i)
        }
        else if (i.isDirectory) stack.push(i)
      }
    }
    arrayBuffer.toArray
  }
}
