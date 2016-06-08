package edu.usc.irds.sparkler.solr

import edu.usc.irds.sparkler.model.SparklerJob
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.TaskContext
import org.slf4j.LoggerFactory
import SolrSink.LOG

import scala.collection.JavaConversions._
/**
  * Created by thammegr on 6/7/16.
  */
class SolrSink(job: SparklerJob) extends ((TaskContext, Iterator[SolrInputDocument]) => Any) with Serializable{

  override def apply(context: TaskContext, docs: Iterator[SolrInputDocument]): Any = {
    LOG.debug("Indexing to Solr")
    val solrClient = job.newCrawlDbSolrClient()
    solrClient.addResourceDocs(docs)
    solrClient.close()
  }
}

object SolrSink {
  val LOG = LoggerFactory.getLogger(SolrSink.getClass)
}
