package org.wa9nnn.fdcluster.javafx

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.CallsignValidator
import org.wa9nnn.fdcluster.model.CallSign

class CallsignSpec extends Specification {

  "Callsign" >> {
    "Validator" >> {
      "Full callSign" in {
        CallsignValidator.valid("WA9NNN") must beNone
      }
      val notCallsignError = beSome("Not callSign!")
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

    "CallSign" >> {
      "lotscallSigns" >> {
        val callsigns = org.wa9nnn.fdcluster.javafx.LotsaCallSigns.cs
        val length = callsigns.length
        var badCount = 0

        callsigns.foreach { cs =>
          try {
            val callSign = CallSign.parse(cs)
            callSign.callSign must beEqualTo(cs)
          } catch {
            case e: MatchError =>
              badCount += 1
              println(s"No match: ${e.getMessage()}")
          }

        }
        println(s"$badCount out of $length are bad!")
        badCount must beEqualTo(1)
      }
      "implicit s2cs" >> {
        import org.wa9nnn.fdcluster.model.CallSign.s2cs
        val cs:CallSign = "WA9NNN"
        cs must beEqualTo (CallSign("WA9NNN"))
      }
    }

  }
}
