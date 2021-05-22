package org.wa9nnn.fdcluster.contest.fieldday

import _root_.com.wa9nnn.util.tableui.{Cell, _}
import org.wa9nnn.fdcluster.model.Station.Band
import org.wa9nnn.fdcluster.model.{AllContestRules, Qso}
import org.wa9nnn.fdcluster.store.QsoSource

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}

@Singleton
class BandModeBreakDown @Inject()(qsoSource: QsoSource, allContestRules: AllContestRules) {

  /**
   * Produces the BandModeBreakDown [[com.wa9nnn.util.tableui.Table]] for the summary HTML page.
   *
   * @param power watts or "kw"
   * @return a [[com.wa9nnn.util.tableui.Table]] that
   */
  def apply(power: String): Table = {
    val powerCell: Cell = Cell(power)
      .withCssClass("sumCell")

    val rows: Seq[Row] = {
      qsoSource.qsoIterator
        .toSeq
        .groupBy(_.bandMode.bandName)
        .map { bq: (Band, Seq[Qso]) =>
          SumBandRow(bq, powerCell).toRow
        }.toSeq
    }

    Table(Seq(
      Seq(Cell("").withCssClass("cornerCell"), Cell("CW", colSpan = 2), Cell("Digital", colSpan = 2), Cell("Phone", colSpan = 2)),
      Seq("Band", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)")
    ), rows)

  }

  case class SumBandRow(bq: (Band, Seq[Qso]), powerCell: Cell) extends RowSource {
    // Start with entry for each possible mode.
    private val modeCounts: Map[String, AtomicInteger] =
      allContestRules.currentRules.modes.modes.map { availableMode =>
        availableMode -> new AtomicInteger()
      }.toMap

    val (band: Band, qsos: Seq[Qso]) = bq

    qsos.foreach { qso =>
      val mode = qso.bandMode.modeName
      modeCounts.get(mode) match {
        case Some(atomicInteger) =>
          atomicInteger.incrementAndGet()
        case None =>
          throw new IllegalArgumentException(s"Unexpected mode: $mode!")
      }
    }

    override def toRow: Row = {
      new Row(Cell(band).withCssClass("sumBand") +:
        modeCounts
          .toSeq
          .sortBy(_._1) //by mode
          .flatMap { t2 =>
            Seq(
              Cell(t2._2.get()).withCssClass("sumCell"),
              powerCell)
          }
      )

    }

  }
}