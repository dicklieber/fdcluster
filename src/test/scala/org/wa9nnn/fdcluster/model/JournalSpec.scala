package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification

import java.time.{ZoneId, ZonedDateTime}

class JournalSpec extends Specification {

  "Journal" >> {
    "happy" >> {
      val instant = ZonedDateTime.of(1949, 12, 15, 0, 0, 5, 0, ZoneId.of("UTC")).toInstant
      val journal = Journal.newJournal("FD", NodeAddress(), instant)

      val name = journal.journalFileName
      name must beEqualTo ("FD19491215.5.json")
    }
  }
}
