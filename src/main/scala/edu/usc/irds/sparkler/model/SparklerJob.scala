package edu.usc.irds.sparkler.model

import java.util.Properties

import edu.usc.irds.sparkler.service.SolrProxy
import edu.usc.irds.sparkler.util.JobUtil
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
  *
  * @since 5/31/16
  */
class SparklerJob extends Serializable {

  import SparklerJob._
  var id:String = _
  var currentTask:String = _
  var settings:Properties = _
  var crawlDbUri:String = _

  def this(id:String, currentTask:String){
    this()
    this.id = id
    this.currentTask = currentTask
    this.settings = SETTINGS
    this.crawlDbUri = settings.getProperty(CRAWLDB_KEY, CRAWLDB_DEFAULT_URI)
  }

  def this(id:String){
    this(id, JobUtil.newSegmentId())
  }

  def newCrawlDbSolrClient():SolrProxy ={
    if (crawlDbUri.startsWith("http://")){
      return new SolrProxy(new HttpSolrClient(crawlDbUri))
    }

    throw new RuntimeException(s"$crawlDbUri not supported")
  }

}

object SparklerJob {

  val CRAWLDB_KEY = "sparkler.crawldb"
  val CRAWLDB_DEFAULT_URI = "http://localhost:8983/solr/crawldb"
  val DEFAULT_CONF = "sparkler-default.properties"
  val OVERRIDDEN_CONF = "sparkler-site.properties"
  val SETTINGS = new Properties()

  private var stream = getClass.getClassLoader.getResourceAsStream(DEFAULT_CONF)
  SETTINGS.load(stream)
  stream.close()

  stream = getClass.getClassLoader.getResourceAsStream(OVERRIDDEN_CONF)
  if (stream != null){
    SETTINGS.load(stream)
    stream.close()
  }
}
