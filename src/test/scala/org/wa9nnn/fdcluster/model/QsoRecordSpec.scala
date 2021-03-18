package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification

class QsoRecordSpec extends Specification {

  "Binary" should {
    "round trip" in {
      val qsoMetadata = QsoMetadata()
      val qso = Qso("WA9NNN", BandMode(), Exchange("1H", "IL"))
      val qsoRecord = QsoRecord(qso, qsoMetadata)
      val dso = DistributedQsoRecord(qsoRecord, NodeAddress(), 42)
      val byteString = dso.toByteString
      val backAgain: DistributedQsoRecord = DistributedQsoRecord(byteString)
      backAgain must beEqualTo(dso)
    }


  }
}
