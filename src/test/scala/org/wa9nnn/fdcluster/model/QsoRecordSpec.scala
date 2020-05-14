package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{BandMode, Contest, DistributedQsoRecord, Exchange, FdLogId, NodeAddress, OurStation, Qso, QsoRecord}

class QsoRecordSpec extends Specification {

  "Binary" should {
    "round trip" in {
      val qsoRecord = QsoRecord(Qso("WA9NNN", BandMode(), Exchange("1A", "IL")), Contest("FD", 2019), OurStation("WM9W"), FdLogId(1, NodeAddress()))
      val dso = DistributedQsoRecord(qsoRecord, NodeAddress(), 42)
      val byteString = dso.toByteString
      val backAgain = DistributedQsoRecord(byteString)
      backAgain must beEqualTo(dso)
    }


  }
}
