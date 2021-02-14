package org.wa9nnn.fdcluster.rig

import org.specs2.mutable.Specification

import java.time.Instant

class RigSettingsSpecs extends Specification {

  "RigSettingsSpecs" should {
    val expected = RigSettings().copy(stamp = Instant.EPOCH)
    "encodeJson" in {
      expected.encodeJson must beEqualTo ("""{"rigModel":{"number":-1,"mfg":"None","model":"-"},"serialPortSettings":{"port":"-","baudrate":"19200"},"stamp":"1970-01-01T00:00:00Z"}""".stripMargin)
    }
    "round trip"  >>{
      val backAgain = RigSettings.decodeJson(expected.encodeJson)
      backAgain must beEqualTo (expected)
    }
  }
}
