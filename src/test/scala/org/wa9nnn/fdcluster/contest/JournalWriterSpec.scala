package org.wa9nnn.fdcluster.contest

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{NodeAddress, Qso, QsoMetadata, QsoRecord}
import play.api.libs.json.Json

import java.nio.file.Files
import scala.jdk.CollectionConverters.CollectionHasAsScala
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.time.Instant

class JournalWriterSpec extends Specification with Mockito {
  val qsoRecord: QsoRecord = org.wa9nnn.fdcluster.tools.MockQso.qsoRecord
  "JournalWriterSpec" >> {
    "happy path" >> {
      val journalStamp = Instant.now()
      val journalProperty = mock[JournalPropertyWriting]
      val ourNodeAddress = NodeAddress()
      val journal = Journal.apply("Test", ourNodeAddress, stamp = journalStamp)
      journalProperty.maybeJournal returns (Some(journal))
      val path = Files.createTempFile(journal.journalFileName, "")
      journalProperty.filePath returns (path)
      val journalWriter = new JournalWriter(journalProperty, ourNodeAddress)
      try {
        journalWriter.write(qsoRecord)
      } catch {
        case e: Exception =>
          throw e
      } finally {

        val lines: List[Node] = Files.readAllLines(path).asScala.toList
        lines must haveLength(2)
        val journalHeader = Json.parse(lines.head).as[JournalHeader]
        journalHeader.journal must beEqualTo(journal)
        journalHeader.ourNodeAddress must beEqualTo(ourNodeAddress)
        Json.parse(lines(1)).as[QsoRecord] must beEqualTo(qsoRecord)

        journalWriter.write(qsoRecord) // write another
        val lines1: List[Node] = Files.readAllLines(path).asScala.toList
        lines1 must haveLength(3)

        Files.delete(path)
      }
      ok
    }
  }
}
