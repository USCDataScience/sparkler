package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.model.Resource
import edu.usc.irds.sparkler.pipeline.SerializableFunction2
import edu.usc.irds.sparkler.util.SolrResultIterator
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}

import scala.collection.JavaConversions._

/**
  *
  * @since 5/28/16
  */
class CrawlDbRDD(sc:SparkContext, solrUrl:String) extends RDD[Resource](sc, Seq.empty) {

  //TODO: accept serializable solr factory

  @DeveloperApi
  override def compute(split: Partition, context: TaskContext): Iterator[Resource] = {
    val partition:SolrGroupPartition = split.asInstanceOf[SolrGroupPartition]
    val batchSize = 20
    val query = new SolrQuery(s"${CrawlDbRDD.QRY_STR} AND group:${partition.group}")
    query.set("sort", CrawlDbRDD.SORT_BY)

    new SolrResultIterator[Resource](new HttpSolrClient(solrUrl), query,
      batchSize, classOf[Resource], closeClient = true)
  }

  override protected def getPartitions: Array[Partition] = {
    val qry = new SolrQuery(CrawlDbRDD.QRY_STR)
    qry.set("sort", CrawlDbRDD.SORT_BY)
    qry.set("group", true)
    qry.set("group.ngroups", true)
    qry.set("group.field", "group")
    qry.set("group.limit", 0)
    qry.setRows(CrawlDbRDD.GROUPS_COUNT)
    val solr = new HttpSolrClient(solrUrl)
    val groupRes = solr.query(qry).getGroupResponse.getValues.get(0)
    val grps = groupRes.getValues
    CrawlDbRDD.LOG.info(s"selecting ${grps.size()} out of ${groupRes.getNGroups}")
    val res = new Array[Partition](grps.size())
    for (i <- 0 until grps.size()){
      res(i) = new SolrGroupPartition(i, grps(i).getGroupValue)
    }
    solr.close()
    res
  }
}


class SolrGroupPartition(val indx:Int, val group:String) extends Partition {
  override def index: Int = indx
}

class SolrSink(solrUrl:String) extends
  SerializableFunction2[TaskContext, Iterator[SolrInputDocument], Any] {

  override def apply(context: TaskContext, docs: Iterator[SolrInputDocument]): Any = {
    println(Thread.currentThread().getName + " :: Indexing to solr")
    val solr = new HttpSolrClient(solrUrl)
    solr.add(docs)
    solr.close()
  }
}

class SolrBeanSink(solr:SolrClient) extends
  SerializableFunction2[TaskContext, Iterator[AnyRef], Any] {

  override def apply(ctx: TaskContext, docs: Iterator[AnyRef]): Any = {
    println(Thread.currentThread().getName + " :: Indexing to solr")
    solr.addBeans(docs)
  }
}


object CrawlDbRDD {

  val LOG = org.slf4j.LoggerFactory.getLogger(classOf[CrawlDbRDD])
  val QRY_STR = "status:NEW"
  val SORT_BY = "depth asc,score asc"
  val GROUPS_COUNT = 100
  val TOP_N = 1000
}
