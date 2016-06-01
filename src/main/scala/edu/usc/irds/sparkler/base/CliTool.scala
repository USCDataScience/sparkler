package edu.usc.irds.sparkler.base

import org.kohsuke.args4j.{CmdLineException, CmdLineParser}

import scala.collection.JavaConversions._
/**
  *
  * @since 5/31/16
  */
trait CliTool extends Runnable {

  val cliParser = new CmdLineParser(this)


  def parseArgs(ags:Array[String]): Unit ={
    try {
      cliParser.parseArgument(ags.toList)
    } catch {
      case e:CmdLineException =>
        System.err.println(e.getMessage)
        cliParser.printUsage(System.err)
        System.exit(1)
    }
  }

  def run(args:Array[String]): Unit ={
    parseArgs(args)
    run()
  }
}
