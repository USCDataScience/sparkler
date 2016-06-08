package edu.usc.irds.sparkler

import edu.usc.irds.sparkler.base.Loggable
import edu.usc.irds.sparkler.pipeline.Crawler
import edu.usc.irds.sparkler.service.Injector

/**
  * Created by thammegr on 6/7/16.
  */
object Main extends Loggable {

  val subCommands = Map[String, (Class[_], String)](
    "inject" -> (classOf[Injector], "Inject (seed) URLS to crawldb"),
    "crawl" -> (classOf[Crawler], "Run crawl pipeline for several iterations")
  )

  def main(args: Array[String]): Unit ={
    if (args.length == 0 || "help".eq(args(0).toLowerCase)){
      println("Sub Commands:")
      for (c <- subCommands) {
        printf("%8s : %s \n%8s - %s\n", c._1, c._2._1.getName, "", c._2._2)
      }
      System.exit(0)
    } else {
      args(0) = args(0).toLowerCase
      if (subCommands.contains(args(0))){
        val method = subCommands(args(0))._1.getMethod("main", args.getClass)
        method.invoke(null, args.slice(1, args.length))
      } else {
        LOG.error(s"ERROR: Command ${args(0)} is unknown. Type 'help' for details")
        System.exit(1)
      }
    }
  }
}
