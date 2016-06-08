package edu.usc.irds.sparkler.pipeline

import java.util.concurrent.atomic.AtomicLong

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.model.{Content, CrawlData, Resource}

/**
  * Created by thammegr on 6/7/16.
  */
class FairFetcher(val resources: Iterator[Resource], val delay: Long,
                  val fetchFunc: (Resource => Content),
                  val parseFunc: ((CrawlData) => Set[String]))
  extends Iterator[CrawlData] {

  import FairFetcher.LOG

  val hitCounter = new AtomicLong()
  var lastHit: String = ""

  override def hasNext: Boolean = resources.hasNext

  override def next(): CrawlData = {

    val data = new CrawlData(resources.next())
    val nextFetch = hitCounter.get() + delay
    val waitTime = nextFetch - System.currentTimeMillis()
    if (waitTime > 0) {
      LOG.debug("    Waiting for {} ms, {}", waitTime, data.res.url)
      Thread.sleep(waitTime)
    }

    //STEP: Fetch
    data.content = fetchFunc(data.res)
    lastHit = data.res.url
    hitCounter.set(System.currentTimeMillis())

    //STEP: Parse
    data.outLinks = parseFunc(data)
    data
  }
}

object FairFetcher extends Loggable {}
