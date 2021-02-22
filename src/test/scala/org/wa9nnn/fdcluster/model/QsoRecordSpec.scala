package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{BandModeOperator, Contest, DistributedQsoRecord, Exchange, FdLogId, NodeAddress, OurStation, Qso, QsoRecord}

class QsoRecordSpec extends Specification {

  "Binary" should {
    "round trip" in {
      val ourStation = OurStation("WM9W")
      val qsoRecord = QsoRecord(Qso("WA9NNN", BandModeOperator(), Exchange("1H", "IL")), Contest("FD", 2019), ourStation, FdLogId(1, NodeAddress()))
      val dso = DistributedQsoRecord(qsoRecord, NodeAddress(), 42)
      val byteString = dso.toByteString
      val backAgain: DistributedQsoRecord = DistributedQsoRecord(byteString)
      backAgain must beEqualTo(dso)
    }


  }
}
