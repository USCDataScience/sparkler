package edu.usc.irds.sparkler.util

import edu.usc.irds.sparkler.model.SparklerJob
import collection.JavaConverters._
object HealthChecks {

  def checkFailureRate(job: SparklerJob): Boolean ={
    if(job.getConfiguration.containsKey("fetcher.kill.failure.percent")) {
      import org.apache.solr.common.params.MapSolrParams
      val solrClient = job.newStorageProxy()

      val q = "crawl_id:" + job.getId
      val queryParamMap : Map[String,String] = Map("q" -> q,
      "facet.field" -> "status", "facet" -> "on", "rows"->"0")

      val queryParams = new MapSolrParams(queryParamMap.asJava)

      val response = solrClient.getClient().query(queryParams)
      val documents = response.getFacetField("status")

      val values = documents.getValues.asScala

      var total : Long = 0
      var err : Long  = 0
      for(v <- values){
        if(v.getName == "ERROR"){
          err = v.getCount
        }
        total = total + v.getCount
      }
      val currenterrrate = err/total*100
      if(currenterrrate > job.getConfiguration.get("fetcher.kill.failure.percent").asInstanceOf[Double]){
        true
      } else{
        false
      }
    } else{
      false
    }
  }
}
