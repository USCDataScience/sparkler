package edu.usc.irds.sparkler.service

import java.io.Closeable

import edu.usc.irds.sparkler.model.Resource
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.common.SolrInputDocument

/**
  *
  * @since 5/28/16
  */
class SolrProxy(var crawlDb:SolrClient) extends Closeable {


  def addResourceDocs(docs:java.util.Iterator[SolrInputDocument]): Unit ={
    crawlDb.add(docs)
  }

  def addResources(beans:java.util.Collection[_]): Unit ={
    crawlDb.addBeans(beans)
  }

  def addResources(beans:java.util.Iterator[_]): Unit = {
    crawlDb.addBeans(beans)
  }

  def commitCrawlDb(): Unit ={
    crawlDb.commit()
  }

  override def close(): Unit = {
    if (crawlDb != null){
      crawlDb.close()
    }
  }
}
