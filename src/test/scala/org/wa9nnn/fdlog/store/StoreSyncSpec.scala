
package org.wa9nnn.fdlog.store

import java.time.{Duration, LocalDateTime}

import org.specs2.mutable.Specification
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdlog.model.{Contest, CurrentStationProviderImpl, NodeAddress}
import org.wa9nnn.fdlog.store.network.FdHour

class StoreSyncSpec extends Specification {
  val expectedNodeAddress = NodeAddress()
  implicit val nodeInfo: NodeInfoImpl = new NodeInfoImpl(
    contest = Contest("WFD", 2017),
    nodeAddress = expectedNodeAddress)

  private val storeMapImpl = new StoreMapImpl(nodeInfo, new CurrentStationProviderImpl())

  private val nQsos = 10000
  private val records = QsoGenerator(nQsos, Duration.ofMinutes(1), LocalDateTime.of(2019, 6, 23, 12, 0, 0))
  records.foreach(qr ⇒
    storeMapImpl.addRecord(qr))

  "nodeStats" should {
    "do good" in {
      val status: NodeStatus = storeMapImpl.nodeStatus
      status.count must beEqualTo(nQsos)
      status.nodeAddress must beEqualTo(expectedNodeAddress)
      status.stamp must not beNull

      val startOfContest: FdHour = status.qsoHourDigests.head.startOfHour
      var currentExpectedHour = startOfContest

      status.qsoHourDigests.foreach { qsoIds: QsoHourDigest ⇒
        currentExpectedHour must beEqualTo(qsoIds.startOfHour)

//        println(qsoIds.startOfHour)

        currentExpectedHour = currentExpectedHour.plus(1)

      }
      ok
    }
  }


}