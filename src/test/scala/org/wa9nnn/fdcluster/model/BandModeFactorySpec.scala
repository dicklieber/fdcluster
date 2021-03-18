package org.wa9nnn.fdcluster.model

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.CurrentStation.Band

class BandModeFactorySpec extends Specification with DataTables {

  "BandModeFactorySpec" should {
    val bandModeFactory = new BandModeFactory()
    "bands" in {
      bandModeFactory.avalableBands must haveSize(10)
    }
    "freq to band in" >> {
      "FreqKHz" | "Band" |
        14000 ! "20m" |
        14313 ! "20m" |
        13000 ! "" |> { (kHz, bandName) => {
        if (bandName.isEmpty)
          bandModeFactory.band(kHz) must beNone
        else {
          bandModeFactory.band(kHz) must beSome(bandName)
        }
      }
      }
    }
    "modes" >> {
      "From Config" >> {
        bandModeFactory.modes must haveSize(3)
        bandModeFactory.modes.head.mode must beEqualTo("CW")
        bandModeFactory.modes(1).mode must beEqualTo("DI")
        bandModeFactory.modes.last.mode must beEqualTo("PH")
      }
      "Convert from rig" >> {
        "Rig" || "Mode" |
          "USB" !! "PH" |
          "USB" !! "PH" |> { (rig, fd) => {
          bandModeFactory.modeForRig(rig) must beSome(fd)
        }

        }
      }
    }
  }
}
