package org.wa9nnn.fdcluster.javafx

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.ContestCallsignValidator

class CallsignSpec extends Specification {

  "CallsignSpec" should {
    "Full callsign" in {
     ContestCallsignValidator.valid("WA9NNN") must beNone
    }
    val notCallsignError = beSome("Not callsign!")
    "No Suffix" in {
     ContestCallsignValidator.valid("WA9") must notCallsignError
    }
   "empty" in {
     ContestCallsignValidator.valid("") must notCallsignError
    }
   "no area" in {
     ContestCallsignValidator.valid("KD") must notCallsignError
    }

  }
}
