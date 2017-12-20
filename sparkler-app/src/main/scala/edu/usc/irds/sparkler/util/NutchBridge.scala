package edu.usc.irds.sparkler.util

import edu.usc.irds.sparkler.model
import edu.usc.irds.sparkler.model.FetchedData
import org.apache.hadoop.conf.Configuration
import org.apache.nutch.metadata.Metadata
import org.apache.nutch.protocol.Content

import scala.collection.JavaConversions._



/**
  * Provides utilities for bridging Nutch and Sparkler
  */
object NutchBridge {

  /**
    * Converts Sparkler's content to Nutch Content model.
    * @param rec Sparkler's content
    * @param conf Hadoop configuration (required by Nutch)
    * @return Nutch's Content
    */
  def toNutchContent(rec: FetchedData, conf: Configuration): Content = {
    new Content(rec.getResource.getUrl, rec.getResource.getUrl, rec.getContent,
      rec.getContentType, toNutchMetadata(rec.getMetadata), conf)
  }

  def toNutchMetadata(meta: model.MultiMap[String, String] ): Metadata  ={
    val mutchMeta  = new Metadata
    for (key <- meta.keySet()){
      for (value <- meta.get(key)) {
        mutchMeta.add(key, value)
      }
    }
    mutchMeta
  }
}
