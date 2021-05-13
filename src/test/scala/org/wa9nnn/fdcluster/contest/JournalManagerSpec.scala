package org.wa9nnn.fdcluster.contest

import org.specs2.matcher.TryFailureMatcher
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.ContestProperty
import org.wa9nnn.fdcluster.store.{ClearStore, StoreSender}
import org.wa9nnn.fdcluster.tools.MockPersistence
import org.wa9nnn.fdcluster.{FileContext, MockFileContext}
import org.wa9nnn.util.Persistence
import play.api.libs.json.{Reads, Writes}

import java.nio.file.Path
import scala.reflect.ClassTag

class JournalManagerSpec extends Specification with Mockito {

  val fileManger: FileContext = MockFileContext()
  private val storeSender: StoreSender = mock[StoreSender]

  private val contestProperty = new ContestProperty(fileManger)
  "JournalManager" >> {
    "initial state" >> {
      val journalManager = new JournalManager(fileManger, contestProperty, storeSender)
      journalManager.journalFilePathProperty.value must beFailedTry.withThrowable[IllegalStateException]
      journalManager._currentJournal must beNone
    }
    "no callsign" >> {
      val journalManager = new JournalManager(fileManger, contestProperty, storeSender)
      journalManager.createNewJournal() must throwA(new IllegalStateException("No CallSign!"))
    }

    "createNewJournal" >> {
      val contestProperty = new ContestProperty(fileManger)
      contestProperty.value = Contest("WM9W")
      val journalManager = new JournalManager(fileManger, contestProperty, storeSender)
      journalManager.createNewJournal()
      val fpp = journalManager.journalFilePathProperty.value
      val newPath = fpp.get
      val newFileName = newPath.getFileName.toString
      val newJournal: Option[Journal] = journalManager._currentJournal
      newJournal.get.journalFileName must beEqualTo (newFileName)
       there was one(storeSender).!(ClearStore)

      // reload from file
      val journalManager2 = new JournalManager(fileManger, contestProperty, storeSender)
      journalManager2._currentJournal must beEqualTo (newJournal)

    }
  }
}
