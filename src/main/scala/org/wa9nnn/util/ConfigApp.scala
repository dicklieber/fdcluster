package org.wa9nnn.util

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import java.nio.file.Paths
import scala.jdk.CollectionConverters.SetHasAsScala
import scala.sys.exit

object ConfigApp extends  StructuredLogging {
  def apply: Config = {
    try {
      val builtinConfig = ConfigFactory.load()
      // LogFilePath must be invoked before any logging happens log fie wont be available and no log file will be generated!
      LogFilePath(builtinConfig)

      val userHome = Paths.get(System.getProperty("user.home"))
      val userConf = userHome.resolve("fdcluster").resolve("user.conf")

      val userConfig = ConfigFactory.parseFile(userConf.toFile)
      val userOrigin = userConfig.origin()
      Option(userOrigin.url()).foreach(url => {
        logger.info(s"config items from: ${userOrigin.url()}")
        dumpConfig(userConfig)
      }
      )

      val finalConfig = userConfig.withFallback(builtinConfig)
      val finalOrigin = finalConfig.origin()
      val parts = finalOrigin.description.split("""\s+@\s+""")
        .filterNot(line => line.startsWith("jar:"))
      parts.foreach(part => logger.info(part))
      logger.info(s"final config:")
      dumpConfig(finalConfig.getConfig("fdcluster"))


      finalConfig
    } catch {
      case e:Throwable =>
        logger.error("loading configuratuib", e)
        exit(1)
    }
  }


  //  @tailrec
  def dumpConfig(config: Config): Unit = {
    config.entrySet().asScala.foreach { entry =>
      entry.getValue match {
        case c: Config =>
          dumpConfig(c)
        case x =>
          logger.info(s"${entry.getKey}:\t${entry.getValue.unwrapped()}")
      }
    }
  }
}