package org.wa9nnn.fdcluster.model

import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.mutable.Specification

import java.net.URL

class EntryCategorySpec extends Specification {
  "EntryCategory" >> {

    val config = ConfigFactory.load()

    val configwfd = ConfigFactory.parseURL(getClass.getResource("/contests/WinterFieldDay.conf")).withFallback(config)
    val configfd = ConfigFactory.parseURL(getClass.getResource("/contests/FieldDay.conf")).withFallback(config)
println(configwfd)

    "parse config line" >> {
      EntryCategory.fromConfig("O: Outdoor") must beEqualTo(EntryCategory("O", "Outdoor"))
    }


  }
}
