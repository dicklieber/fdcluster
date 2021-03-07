package org.wa9nnn.fdcluster.javafx

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.CallsignValidator

class CallsignSpec extends Specification {

  "CallsignSpec" should {
    "Full callsign" in {
      CallsignValidator.valid("WA9NNN") must beNone
    }
    val notCallsignError = beSome("Not callsign!")
    "No Suffix" in {
      CallsignValidator.valid("WA9") must notCallsignError
    }
   "empty" in {
     CallsignValidator.valid("") must notCallsignError
    }
   "no area" in {
     CallsignValidator.valid("KD") must notCallsignError
    }

  }
}
