
package org.wa9nnn.fdcluster.store

import org.apache.commons.io.FileUtils
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach
import org.wa9nnn.fdcluster.javafx.sync.SyncSteps
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.tools.{GenerateRandomQsos, RandomQsoGenerator}
import org.wa9nnn.fdcluster.{FileManager, MockFileManager}
import org.wa9nnn.util.{CommandLine, DebugTimer}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.nio.file.{Files, Path}


class StoreSyncSpec extends Specification with BeforeAfterEach with DebugTimer with Mockito {
  sequential
  private val nQsos = 10000
  val mockSyncSteps: SyncSteps = mock[SyncSteps]
  val expectedNodeAddress: NodeAddress = NodeAddress()

  private var storeMapImpl: StoreMapImpl = _
  private var records: List[QsoRecord] = _

  "nodeStats" should {
    "do good" in {
      val status: NodeStatus = storeMapImpl.nodeStatus
      status.qsoCount must beEqualTo(nQsos)
      status.nodeAddress must beEqualTo(expectedNodeAddress)

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
        val firstHour = records.head.fdHour
        val thirdHour = firstHour.plus(2)
        val uuids = storeMapImpl.uuidForHours(Set(firstHour, thirdHour))
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
        val alreadyInNode = records(10).qso.uuid
        val missing = storeMapImpl.missingUuids(List("other1", alreadyInNode, "other2"))
        missing must contain("other1")
        missing must contain("other2")
        missing must not contain alreadyInNode
        missing must haveSize(2)
      }
    }
  }
  val directory: Path = Files.createTempDirectory("StoreMapImplSpec")
  val fileManager: FileManager = MockFileManager()
  val journalPath: Path = directory.resolve("journal.json")


  override def before(): Unit = {
    val commandLine: CommandLine = mock[CommandLine].is("skipJournal") returns false

    storeMapImpl = {
      println("Creating store")
      val allQsos = new ObservableBuffer[QsoRecord]()
      new StoreMapImpl(NodeAddress(),
        ObjectProperty[QsoMetadata](QsoMetadata()),
        new ObservableBuffer[QsoRecord](),
        mockSyncSteps, fileManager
      )
    }

    val randomQsoGenerator = new RandomQsoGenerator()
    val builder = List.newBuilder[QsoRecord]
    randomQsoGenerator.apply(GenerateRandomQsos()) {builder += QsoRecord(_, QsoMetadata())
    }
    records = builder.result()
    debugTime(s"load ${records.size} in $$dur") {
      records.foreach(qr ⇒
        storeMapImpl.addRecord(qr))
    }
  }

  override def after: Unit = {
    FileUtils.deleteDirectory(directory.toFile)
  }
}