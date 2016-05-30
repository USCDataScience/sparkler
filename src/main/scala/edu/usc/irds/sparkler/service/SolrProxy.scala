package edu.usc.irds.sparkler.service

import org.apache.solr.client.solrj.SolrClient

/**
  *
  * @since 5/28/16
  */
class SolrProxy {

  var solr:SolrClient = _

  def addBeans(beans:java.util.Collection[_]): Unit ={
    solr.addBeans(beans)
  }

  def commit(): Unit ={
    solr.commit()
  }

}
