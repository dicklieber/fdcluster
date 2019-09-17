
package org.wa9nnn.fdlog.store

import java.time.{Duration, LocalDateTime}
import java.util.UUID

import org.wa9nnn.fdlog.javafx.entry.Sections
import org.wa9nnn.fdlog.model._

import scala.util.Random

object QsoGenerator {
  def apply(numberfOfQsos: Int, betweewnQsos: Duration, startOfContest: LocalDateTime): List[QsoRecord] = {
    var iteration = 0
    val secondsBetween = betweewnQsos.toSeconds
    for {
      area ← (0 to 9).toList
      suffix1 ← 'A' to 'Z'
      suffix2 ← 'A' to 'Z'
      suffix3 ← 'A' to 'Z'
      if iteration < numberfOfQsos

    } yield {
      iteration = iteration + 1
      val callsign = s"WA$area$suffix1$suffix2$suffix3"
      val qso = Qso(callsign, bandMode, exchange, startOfContest.plusSeconds(secondsBetween * iteration))
      val fdLOgId = FdLogId(nodeSn = iteration,
        nodeAddress = NodeAddress(0, "10.10.10.1"),
        uuid = UUID.randomUUID().toString)
      QsoRecord(contest, ourStation, qso, fdLOgId)
    }
  }

  private val random = new Random(42)

  private val contest = Contest("UT", 2019)
  private val ourStation = OurStation("N9VTB", "IC-7300", "vdipole")

  private def exchange: Exchange = {
    val section = Sections.sections(random.nextInt(Sections.sections.size - 1)).code
    Exchange("1A", section)

  }

  private def bandMode: BandMode = {
    val band: Band = Band(Band.bands(random.nextInt(Band.bands.size - 1)))
    val mode = Mode.values()(random.nextInt(3))
    BandMode(band, mode)
  }
}
