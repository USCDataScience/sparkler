package edu.usc.irds.sparkler.solr

import org.apache.spark.Partition

/**
  * Created by thammegr on 6/7/16.
  */
class SolrGroupPartition(val indx: Int, val group: String, val start: Int = 0,
                         val end: Int = Int.MaxValue) extends Partition {
  override def index: Int = indx
}
