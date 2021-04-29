package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.model.{BandMode, Exchange, Qso, QsoMetadata, QsoRecord}

import java.time.Instant
import java.util.UUID


object MockQso {
  val qso: Qso = Qso("WA9NNN", BandMode(), Exchange(), Instant.EPOCH, UUID.fromString("3d85f8b2-192b-49da-b00e-ef3a303abf45"))
  val qsoRecord: QsoRecord = QsoRecord(qso, QsoMetadata())
}
