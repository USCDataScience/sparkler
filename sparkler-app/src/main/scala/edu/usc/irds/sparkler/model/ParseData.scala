package edu.usc.irds.sparkler.model

import org.apache.tika.metadata.Metadata

/**
  * Created by karanjeetsingh on 9/12/16.
  */
class ParseData extends Serializable {
  var plainText: String = _
  var outlinks: Set[String] = Set.empty[String]
  var metadata: Metadata = new Metadata()
}
