
package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.util.DebugTimer
import scalafx.collections.ObservableBuffer

import java.time.{Duration, LocalDateTime}


class StoreSyncSpec extends Specification with BeforeEach with DebugTimer {
  sequential
  private val nQsos = 10000

  val expectedNodeAddress = NodeAddress()
  implicit val nodeInfo: NodeInfoImpl = new NodeInfoImpl(
    contest = Contest("WFD", 2017),
    nodeAddress = expectedNodeAddress)

  private var storeMapImpl: StoreMapImpl = _
  private var records: List[QsoRecord] = _

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

    "uuidForQsos" >> {
      "someFdHours" >> {
        var firstrHour = records.head.fdHour
        var thirdrHour = firstrHour.plus(2)
        val uuids = storeMapImpl.uuidForHours(Set(firstrHour, thirdrHour))
        uuids must haveSize(119)
      }
      "allFdHours" >> {
        val uuids = storeMapImpl.uuidForHours(Set.empty)
        uuids must haveSize(nQsos)
      }
    }

    "missing Uuids" >> {
      "all missing" >> {
        val missing = storeMapImpl.missingUuids(List("other1", "other2"))
        missing must contain("other1")
        missing must contain("other2")
        missing must haveSize(2)
      }
      "some already in store" >> {
        val alredyInNode = records(10).uuid
        val missing = storeMapImpl.missingUuids(List("other1", alredyInNode, "other2"))
        missing must contain("other1")
        missing must contain("other2")
        missing must not contain (alredyInNode)
        missing must haveSize(2)
      }
    }
  }

  override def before(): Unit = {
    storeMapImpl = {
      println("Creating store")
      val allQsos = new ObservableBuffer[QsoRecord]()
      new StoreMapImpl(nodeInfo,
        new OurStationStore(org.wa9nnn.fdcluster.PreferencesTest.preferences),
        new BandModeStore(org.wa9nnn.fdcluster.PreferencesTest.preferences),
        allQsos)
    }
    val startTime = LocalDateTime.of(2019, 6, 23, 12, 0, 0)

    records = QsoGenerator(nQsos, Duration.ofMinutes(1), startTime)
    debugTime(s"load ${records.size} in $$dur") {
      records.foreach(qr ⇒
        storeMapImpl.addRecord(qr))
    }
  }
}