package org.wa9nnn.fdlog.javafx

import org.specs2.mutable.Specification

class CallsignSpec extends Specification {

  "CallsignSpec" should {
    "Full callsign" in {
     Callsign.isCallsign("WA9NNN") must beTrue
    }
    "No Suffix" in {
     Callsign.isCallsign("WA9") must beFalse
    }
   "empty" in {
     Callsign.isCallsign("") must beFalse
    }
   "no area" in {
     Callsign.isCallsign("KD") must beFalse
    }

  }
}
