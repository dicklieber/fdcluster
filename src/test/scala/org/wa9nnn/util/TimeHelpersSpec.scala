package org.wa9nnn.util

import org.specs2.mutable.Specification

import java.time.Instant

class TimeHelpersSpec extends Specification {

  "TimeHelpersSpec" should {
    "localFrom" in {
      val instant = Instant.ofEpochSecond(1615485840)
      val str = org.wa9nnn.util.TimeHelpers.localFrom(instant)
      str must beEqualTo ("03/11/21 12:04:00 CST")
      ok
    }
  }
}
