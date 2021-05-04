package org.wa9nnn.fdcluster.model

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class BandFactorySpec extends Specification with DataTables {

  "BandFactorySpec" should {
    val bandFactory = new BandFactory()
    "bands" in {
      bandFactory.availableBands must haveSize(10)
    }
    "freq to band in" >> {
      "FreqKHz" | "Band" |
        7300 ! "40m" |
        14000 ! "20m" |
        14313 ! "20m" |
        13000 ! "" |> { (kHz, bandName) => {
        if (bandName.isEmpty)
          bandFactory.band(kHz * 1000) must beNone
        else {
          bandFactory.band(kHz * 1000) must beSome(bandName)
        }
      }
      }
    }
  }
}
