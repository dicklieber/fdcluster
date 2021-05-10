package org.wa9nnn.fdcluster.rig

import org.specs2.mutable.Specification

class RigctldCommandSpec extends Specification {

  "RigctldCommand" should {
    "apply" in {
      val rigSettings = RigSettings(port = Option(SerialPort(42)), rigModel = RigModel(142))
      val cmdLine = RigctldCommand( rigSettings)
      cmdLine must beEqualTo ("rigctld -m 142  -s 9600 -r COM42")
    }
  }
}
