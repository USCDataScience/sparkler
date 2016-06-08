package edu.usc.irds.sparkler.pipeline

import java.util.Date

import edu.usc.irds.sparkler.model.{CrawlData, Resource}
import org.apache.solr.common.SolrInputDocument

import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  */
object StatusUpdateSolrTransformer extends (CrawlData => SolrInputDocument ) with Serializable {

  override def apply(data: CrawlData): SolrInputDocument = {
    val sUpdate = new SolrInputDocument()
    //FIXME: handle failure case
    //val x:java.util.Map[String, Object] = Map("ss" -> new Object).asJava
    sUpdate.setField(Resource.ID, data.res.id)
    sUpdate.setField(Resource.STATUS, Map("set" -> data.content.status.toString).asJava)
    sUpdate.setField(Resource.LAST_FETCHED_AT, Map("set" -> data.content.fetchedAt).asJava)
    sUpdate.setField(Resource.LAST_UPDATED_AT, Map("set" -> new Date()).asJava)
    sUpdate.setField(Resource.NUM_TRIES, Map("inc" -> 1).asJava)
    sUpdate.setField(Resource.NUM_FETCHES, Map("inc" -> 1).asJava)
    sUpdate
  }
}
