package org.wa9nnn.fdcluster.logging

import com.typesafe.scalalogging.Logger
import net.logstash.logback.argument.StructuredArgument
import org.slf4j.LoggerFactory


trait Loggable {
  val logger: Logger = Logger(LoggerFactory.getLogger(getClass.getName.split("""\.""").last))
  //  var logMetadata: Option[LogMetadata] = None
//  var localLog = LocalLog.nolocal

//  def log(message: String, localLog: LocalLog, args: StructuredArgument*): Unit = {
//    logMetadata = Option {
//      val va: Seq[StructuredArgument] = args :+ kv("local", localLog.localflag)
//      LogMetadata(message, va)
//    }
//  }


  def log(): Unit  //{
//    logMetadata.foreach { logMetadata =>
//      val logger: Logger = Logger(LoggerFactory.getLogger(loggerName))
//
//
//      val args1: Seq[StructuredArgument] = logMetadata.args
//      logger.info(logMetadata.message, args1: _*)
//    }
//  }
//  def log(message:String,  args:StructuredArgument*):Unit = {
//    val logger: Logger = Logger(LoggerFactory.getLogger(loggerName))
//
//    val finalArgs: Seq[StructuredArgument] = buildArgs(args)
//    logger.info(message, finalArgs:_*)
//
//
//  }
//  private def buildArgs(args:Seq[StructuredArgument]):Seq[StructuredArgument] = {
//    localLog match {
//      case LocalLog.local =>
//        args
//      case LocalLog.nolocal =>
//        args :+ kv("local", localLog.localflag)
//    }
//  }

//  var args: Seq[StructuredArgument] = Seq.empty
}


case class LogMetadata(message: String, args: Seq[StructuredArgument] = Seq.empty)
