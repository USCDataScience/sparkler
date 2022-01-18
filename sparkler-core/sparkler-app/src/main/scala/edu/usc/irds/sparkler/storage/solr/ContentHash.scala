package edu.usc.irds.sparkler.storage.solr

object ContentHash {

  def fetchHash(a: Array[Byte]): String = {
    import java.security.MessageDigest
    if (a.length > 0) {
      val md5hash = MessageDigest.getInstance("MD5").digest(a)
      val contentHash = toHexString(md5hash)
      contentHash
    } else{
      ""
    }
  }

  private def toHexString(bytes: Array[Byte]) = {
    val hexString = new StringBuilder
    for (aByte <- bytes) {
      val hex = Integer.toHexString(0xFF & aByte)
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex)
    }
    hexString.toString
  }
}
