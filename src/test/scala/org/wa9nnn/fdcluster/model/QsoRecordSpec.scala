package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.tools.MockQso

class QsoRecordSpec extends Specification {
  "QsoRecord" >> {
    val qso = MockQso.qso.copy(callSign = "WA9NNN")
    "json line round trip" >> {
      val jsonLine = qso.toJsonLine
      val backAgain = Qso(jsonLine)
      backAgain must beEqualTo(qso)
    }
    "json pretty round trip" >> {
      val json = qso.toJsonPretty
      val backAgain = Qso(json)
      backAgain must beEqualTo(qso)
    }
  }
}
