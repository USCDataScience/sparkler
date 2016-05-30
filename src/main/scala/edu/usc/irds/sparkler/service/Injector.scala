package edu.usc.irds.sparkler.service

import java.io.File
import java.net.URL
import java.util

import edu.usc.irds.sparkler.model.Resource
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import Injector.LOG
import org.apache.solr.client.solrj.impl.{CloudSolrClient, HttpSolrClient}
/**
  *
  * @since 5/28/16
  */
class Injector extends Runnable {

  var seedPath:File = _
  var seedUrls:util.Collection[String] = _
  var solr:SolrProxy = _

  override def run(): Unit = {
    val seed = new util.ArrayList[Resource]()
    if (seedUrls != null) {
      for (urlStr <- seedUrls) {
        var depth = 0
        var url:URL = null
        if (urlStr.contains(",")) {
          val parts = urlStr.split(",")
          url = new URL(parts(0))
          depth = parts(1).trim.toInt
        } else {
          url = new URL(urlStr)
        }
        val resource: Resource = new Resource(url.toString, url.getHost)
        resource.depth = depth
        seed.add(resource)
      }
    }
    //TODO: read from file/directory
    LOG.info("Injecting {} seeds", seed.size())
    solr.addBeans(seed)
    solr.commit()
  }
}

object Injector{

  val LOG = LoggerFactory.getLogger(classOf[Injector])

  def main(args: Array[String]) {

    val injector = new Injector
    injector.solr = new SolrProxy()
    if (false){
      val sss = new CloudSolrClient("localhost:9983")
      sss.setDefaultCollection("crawldb")
      injector.solr.solr = sss
    } else {
      injector.solr.solr = new HttpSolrClient("http://localhost:8983/solr/crawldb")
    }

    injector.seedUrls = List("http://www.google.com/", "https://twitter.com/", "http://wordpress.com/")
    injector.run()
    println("===Done===")
  }
}
