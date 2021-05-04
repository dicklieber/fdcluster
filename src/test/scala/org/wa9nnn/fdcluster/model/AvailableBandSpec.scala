package org.wa9nnn.fdcluster.model

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class AvailableBandSpec extends Specification with DataTables{

  "AvailableBand" >> {
    val availableBand = AvailableBand("160m: 1.800 to  2.000")
    "apply" >> {
      availableBand.band must beEqualTo ("160m")
      availableBand.freqStart must beEqualTo (1800000)
      availableBand.freqEnd must beEqualTo (2000000)
    }

    "contains" >> {
      availableBand.containsHz(100) must beFalse
      availableBand.containsHz(1800000 -1) must beFalse
      availableBand.containsHz(1800000) must beTrue
      availableBand.containsHz(2000000) must beTrue
      availableBand.containsHz(2000001) must beFalse
    }
  }
}
