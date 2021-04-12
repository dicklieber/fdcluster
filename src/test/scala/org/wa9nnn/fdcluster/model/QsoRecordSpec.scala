package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification
import MessageFormats._
class QsoRecordSpec extends Specification {
"QsoRecord" >> {
  val qsoRecord = QsoRecord(Qso("WA9NNN", BandMode(), new Exchange()), QsoMetadata())
  "json line round trip" >> {
    val jsonLine = qsoRecord.toJsonLine
    val backAgain = QsoRecord(jsonLine)
    backAgain must beEqualTo (qsoRecord)
  }
  "json pretty round trip" >> {
    val json = qsoRecord.toJsonPretty
    val backAgain = QsoRecord(json)
    backAgain must beEqualTo (qsoRecord)
  }
}
}
