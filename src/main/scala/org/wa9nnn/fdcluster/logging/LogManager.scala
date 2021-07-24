package org.wa9nnn.fdcluster.logging

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{AsyncAppender, Level, Logger}
import ch.qos.logback.core.FileAppender
import net.logstash.logback.appender.LogstashTcpSocketAppender
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.fieldnames.LogstashFieldNames
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.model.NodeAddress
import net.logstash.logback.fieldnames.LogstashCommonFieldNames.IGNORE_FIELD_INDICATOR
import org.wa9nnn.util.HostPort

import scala.jdk.CollectionConverters.CollectionHasAsScala

class LogManager(fileContext: FileContext, nodeAddress: NodeAddress) {


  import ch.qos.logback.classic.LoggerContext
  import org.slf4j.LoggerFactory

  private val logCtx: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  logCtx.reset()

  private val consolePattern: PatternLayoutEncoder = new PatternLayoutEncoder()

  consolePattern.setContext(logCtx)
  consolePattern.setPattern("%d{MM-dd HH:mm:ss} %highlight(%-5level) %cyan(%logger{15}) - %msg %n")
  consolePattern.start()

  import ch.qos.logback.core.ConsoleAppender

  val logConsoleAppender = new ConsoleAppender[ILoggingEvent]
  logConsoleAppender.setContext(logCtx)
  logConsoleAppender.setName("console")
  logConsoleAppender.setEncoder(consolePattern)
  private val logStashFilter = new LogStashFilter
  logConsoleAppender.addFilter(logStashFilter)
  logConsoleAppender.start()

  private val filePatttern: PatternLayoutEncoder = new PatternLayoutEncoder()

  filePatttern.setContext(logCtx)
  filePatttern.setPattern("%d{MM-dd HH:mm:ss} %-5level %logger{0} - %msg %n")
  filePatttern.start()

  private val fileAppender: FileAppender[ILoggingEvent] = new FileAppender[ILoggingEvent]
  fileAppender.setFile(fileContext.logFile.toString)
  fileAppender.setContext(logCtx)
  fileAppender.setName("FILE")
  fileAppender.setEncoder(filePatttern)
  fileAppender.addFilter(logStashFilter)

  fileAppender.start()

  private val asyncAppender = new AsyncAppender
  asyncAppender.addAppender(fileAppender)
  asyncAppender.start()

  private val logstashTcpSocketAppender = new LogstashTcpSocketAppender()
  logstashTcpSocketAppender.addDestination("127.0.0.1:5046")


  val rootLogger: Logger = logCtx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
  rootLogger.setAdditive(true)
  rootLogger.setLevel(Level.INFO)
  rootLogger.addAppender(logConsoleAppender)
  rootLogger.addAppender(asyncAppender)

  private val prg_wa9nnnLogger: Logger = logCtx.getLogger("org.wa9nnn.fdcluster")
  prg_wa9nnnLogger.setLevel(Level.INFO)
  //  prg_wa9nnnLogger.setAdditive()
  val loggers: Iterable[Logger] =
    for {
      logger: Logger <- logCtx.getLoggerList.asScala
    } yield {
      logger
    }

  loggers.foreach { logger =>
    val name: String = logger.getName
    val level: Level = logger.getLevel
    val clazz = logger.getClass
    println(s"logger: $name level: $level class: $clazz")
  }

  var maybeLogstashTcpSocketAppender: Option[LogstashTcpSocketAppender] = None

  /**
   *
   * @param destination host:port
   */
  def startLogstash(destination: HostPort): Unit = {
    stopLogstash()

    val logstashEncoder = new LogstashEncoder()
    val fieldNames: LogstashFieldNames = logstashEncoder.getFieldNames
    fieldNames.setThread(IGNORE_FIELD_INDICATOR)
    fieldNames.setLevelValue(IGNORE_FIELD_INDICATOR)
    logstashEncoder.setFieldNames(fieldNames)
    logstashEncoder.setCustomFields(s"""{"nodeAddress":"${nodeAddress.display}"}""")

    val logstash = new LogstashTcpSocketAppender
    logstash.setName("logstash")
    logstash.setContext(logCtx)
    logstash.addDestination(destination.toString)
    logstash.setEncoder(logstashEncoder)

    logstash.start()
    rootLogger.addAppender(logstash)

    maybeLogstashTcpSocketAppender = Option(logstash)
    rootLogger.addAppender(logstash)
  }

  def stopLogstash(): Unit = {
    maybeLogstashTcpSocketAppender.foreach { appender =>
      appender.stop()
      rootLogger.detachAppender(appender)
    }

  }

  var logstashDest: EnabledDestination = EnabledDestination(HostPort("127.0.0.1", 5044))

  def logstash(logstash: EnabledDestination): Unit = {
    if (logstash != logstashDest) {
      if (logstash.enabled)
        startLogstash(logstash.hostPort)
      else {
        stopLogstash()
        logstashDest = logstash
      }
    }
  }


}
