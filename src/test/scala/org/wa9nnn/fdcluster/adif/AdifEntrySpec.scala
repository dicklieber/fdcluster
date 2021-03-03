package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

class AdifEntrySpec extends Specification {

  "AdifEntrySpec" should {
    "equals same tag case" in {
      val v1 = AdifEntry("CALL", "WA9NNN")
      val v2 = AdifEntry("CALL", "WA9NNN")
      v1 must beEqualTo (v2)
    }
    "equals bad tag case" in {
      AdifEntry("call", "WA9NNN") must throwAn[AssertionError]
    }
    "equals differing " in {
      val v1 = AdifEntry("CALL", "WA9NNN")
      val v2 = AdifEntry("CLASS", "1H")
      v1 must not equalTo  (v2)
    }
  }
}
