package org.wa9nnn.util

import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification

class LogFilePathSpec extends Specification {

  "LogFilePath" should {
    "apply" in {
      val config = ConfigFactory.load()
      System.getProperty("log.file.path", "notset") must beEqualTo("notset")
      LogFilePath(config)
      System.getProperty("log.file.path", "notset") must endWith("/logs/fdcluster.log")
    }
  }
}
