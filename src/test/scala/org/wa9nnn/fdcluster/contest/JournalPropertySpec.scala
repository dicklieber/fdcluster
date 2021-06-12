package org.wa9nnn.fdcluster.contest

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{ContestProperty, Journal, NodeAddress}
import org.wa9nnn.fdcluster.store.{ClearStore, StoreSender}
import org.wa9nnn.fdcluster.{FileContext, MockFileContext}

import java.time.Instant

class JournalPropertySpec extends Specification with Mockito {

  val fileManger: FileContext = MockFileContext()
  private val storeSender: StoreSender = mock[StoreSender]

  private val contestProperty = new ContestProperty(fileManger, NodeAddress())
  "JournalManager" >> {
    "initial state" >> {
      val journalManager = new JournalProperty(fileManger, contestProperty, storeSender, NodeAddress())
      journalManager.journalFilePathProperty.value must beFailedTry.withThrowable[IllegalStateException]
      journalManager.value.stamp must beEqualTo (Instant.EPOCH)
    }
    "no callsign" >> {
      val journalManager = new JournalProperty(fileManger, contestProperty, storeSender, NodeAddress())
      journalManager.createNewJournal() must throwA(new IllegalStateException("No CallSign!"))
    }

    "createNewJournal" >> {
      val contestProperty = new ContestProperty(fileManger, NodeAddress())
      contestProperty.value = Contest("WM9W")
      val journalManager = new JournalProperty(fileManger, contestProperty, storeSender, NodeAddress())
      journalManager.createNewJournal()
      val fpp = journalManager.journalFilePathProperty.value
      val newPath = fpp.get
      val newFileName = newPath.getFileName.toString
      val newJournal: Journal = journalManager.value
      newJournal.journalFileName must beEqualTo (newFileName)
       there was one(storeSender).!(ClearStore)

      // reload from file
      val journalManager2 = new JournalProperty(fileManger, contestProperty, storeSender, NodeAddress())
      journalManager2.value must beEqualTo (newJournal)

    }
  }
}
