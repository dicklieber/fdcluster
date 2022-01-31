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

    "without frequency" >> {
      val cabFreq: String = qso.cabFreq
      cabFreq must beEqualTo("14035")
    }
    "with frequency" >> {
      val q = qso.copy(mHz = Option(224.52F))
      val cabFreq = q.cabFreq
      cabFreq must beEqualTo("224520")
    }
  }
}
