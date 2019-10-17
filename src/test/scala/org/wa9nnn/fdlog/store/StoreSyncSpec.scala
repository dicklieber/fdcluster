
package org.wa9nnn.fdlog.store

import java.time.{Duration, LocalDateTime}

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdlog.model.{Contest, CurrentStationProviderImpl, NodeAddress, QsoRecord}
import org.wa9nnn.fdlog.store.network.FdHour
import scalafx.collections.ObservableBuffer


class StoreSyncSpec extends Specification with BeforeEach {
  private val nQsos = 10000

  val expectedNodeAddress = NodeAddress()
  implicit val nodeInfo: NodeInfoImpl = new NodeInfoImpl(
    contest = Contest("WFD", 2017),
    nodeAddress = expectedNodeAddress)

  private var storeMapImpl: StoreMapImpl = _

  "nodeStats" should {
    "do good" in {
      val status: NodeStatus = storeMapImpl.nodeStatus
      status.qsoCount must beEqualTo(nQsos)
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
    "random killer" >> {
      val sizeB4 = storeMapImpl.size
      storeMapImpl.debugKillRandom(1)
      val sizeAfter = storeMapImpl.size
      sizeAfter must beEqualTo(sizeB4 - 1)
    }

  }

  override def before(): Unit = {
    storeMapImpl = {
      println("Creating store")
      val allQsos = new ObservableBuffer[QsoRecord]()
      new StoreMapImpl(nodeInfo, new CurrentStationProviderImpl(), allQsos)
    }
    val records = QsoGenerator(nQsos, Duration.ofMinutes(1), LocalDateTime.of(2019, 6, 23, 12, 0, 0))
    records.foreach(qr ⇒
      storeMapImpl.addRecord(qr))

    assert(storeMapImpl.size == nQsos)
  }
}