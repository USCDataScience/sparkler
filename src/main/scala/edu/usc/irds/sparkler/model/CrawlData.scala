package edu.usc.irds.sparkler.model

/**
  *
  * @since 5/29/16
  */
class CrawlData (val res: Resource) extends Serializable{
    var content:Content = _
    var outLinks:Set[String]=_
}
