package edu.usc.irds.sparkler.pipeline

import java.io.ByteArrayInputStream

import edu.usc.irds.sparkler.model.CrawlData
import org.apache.tika.metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.LinkContentHandler

import scala.collection.JavaConverters._

/**
  * Created by thammegr on 6/7/16.
  */
object ParseFunction extends ((CrawlData) => Set[String]) with Serializable {

  override def apply(data: CrawlData): Set[String] = {
    val stream = new ByteArrayInputStream(data.content.content)
    val linkHandler = new LinkContentHandler()
    val parser = new AutoDetectParser()
    val meta = new metadata.Metadata()
    meta.set("resourceName", data.content.url)
    parser.parse(stream, linkHandler, meta)
    stream.close()
    linkHandler.getLinks.asScala.map(_.getUri.trim).filter(!_.isEmpty).toSet
  }
}
