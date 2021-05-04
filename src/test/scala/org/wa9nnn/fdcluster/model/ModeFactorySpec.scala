package org.wa9nnn.fdcluster.model

import com.typesafe.config.ConfigFactory
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

 class ModeFactorySpec extends Specification with DataTables{
  "modes" >> {
    val modeFactory = new ModeFactory()
    "From Config" >> {
      modeFactory.modes must haveSize(3)
      modeFactory.modes.head must beEqualTo("CW")
      modeFactory.modes(1) must beEqualTo("DI")
      modeFactory.modes.last must beEqualTo("PH")
      modeFactory.defaultMode must beEqualTo ("DI")
    }
    "Convert from rig" >> {
      "Rig" || "Mode" |
        "USB" !! "PH" |
        "CRAP" !! "DI" |
        "USB" !! "PH" |> { (rig, fd) => {
        modeFactory.modeForRig(rig) must beEqualTo(fd)
      }
      }
      "default from star" >> {
        val config = ConfigFactory.parseString(
          """contest{
            |modes {
            |    PH = "AM FM SSB USB LSB"
            |    CW = "CW"
            |    DI = "PKTUSB USB-D"
            |    XX = "*" // * anything else
            |  }
            |  bands = []
            |}""".stripMargin)

        val bmf = new ModeFactory(config)
        bmf.modes must haveLength(4)
        bmf.modeForRig("CW") must beEqualTo ("CW")
        bmf.modeForRig("USB-D") must beEqualTo ("DI")
        bmf.modeForRig("CRAP") must beEqualTo ("XX")
        bmf.defaultMode must beEqualTo ("XX")

      }
    }
  }

}
