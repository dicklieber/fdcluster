package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.model.{BandMode, Exchange, Qso, QsoMetadata}

import java.time.Instant
import java.util.UUID


object MockQso {
  val qso: Qso = Qso("WA9NNN", Exchange(), BandMode(), QsoMetadata(), Instant.EPOCH, UUID.fromString("3d85f8b2-192b-49da-b00e-ef3a303abf45"))
}
