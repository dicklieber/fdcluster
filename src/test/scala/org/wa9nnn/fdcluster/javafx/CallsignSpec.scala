package org.wa9nnn.fdcluster.javafx

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.ContestCallsign

class CallsignSpec extends Specification {

  "CallsignSpec" should {
    "Full callsign" in {
     ContestCallsign.valid("WA9NNN") must beTrue
    }
    "No Suffix" in {
     ContestCallsign.valid("WA9") must beFalse
    }
   "empty" in {
     ContestCallsign.valid("") must beFalse
    }
   "no area" in {
     ContestCallsign.valid("KD") must beFalse
    }

  }
}
