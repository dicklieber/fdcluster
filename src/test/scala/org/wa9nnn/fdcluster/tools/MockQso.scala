package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.model.{BandMode, Exchange, Qso, QsoMetadata}

import java.time.Instant
import java.util.UUID


object MockQso {
  val qso: Qso = Qso(callSign = "WA9NNN", exchange = Exchange(), bandMode = BandMode(), qsoMetadata = QsoMetadata(), stamp = Instant.EPOCH, uuid = UUID.fromString("3d85f8b2-192b-49da-b00e-ef3a303abf45"))
}
