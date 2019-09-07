
package org.wa9nnn.fdlog.store

import java.net.InetAddress
import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

import org.specs2.mutable.Specification
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHourIds}
import org.wa9nnn.fdlog.model.{Contest, CurrentStationProviderImpl}

class StoreSyncSpec extends Specification {
  val expectedNodeAddress: String = InetAddress.getLocalHost.getHostAddress
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

      val startOfContest = status.qsoIds.head.startOfHour
      var currentExpectedHour = startOfContest

      status.qsoIds.foreach { qsoIds: QsoHourIds ⇒
        currentExpectedHour must beEqualTo(qsoIds.startOfHour)

//        println(qsoIds.startOfHour)

        currentExpectedHour = currentExpectedHour.plus(1)

      }
      ok
    }
  }


}