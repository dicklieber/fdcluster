package org.wa9nnn.fdcluster.javafx

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.ContestCallsignValidator

class CallsignSpec extends Specification {

  "CallsignSpec" should {
    "Full callsign" in {
     ContestCallsignValidator.valid("WA9NNN") must beNone
    }
    "No Suffix" in {
     ContestCallsignValidator.valid("WA9") must beNone
    }
   "empty" in {
     ContestCallsignValidator.valid("") must beNone
    }
   "no area" in {
     ContestCallsignValidator.valid("KD") must beNone
    }

  }
}
