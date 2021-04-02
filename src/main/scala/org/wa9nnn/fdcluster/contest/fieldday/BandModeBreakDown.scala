package org.wa9nnn.fdcluster.contest.fieldday

import com.google.inject.name.Named
import com.wa9nnn.util.tableui.Cell
import _root_.com.wa9nnn.util.tableui._
import org.wa9nnn.fdcluster.model.CurrentStation.Band
import org.wa9nnn.fdcluster.model.{BandModeFactory, QsoRecord}
import scalafx.collections.ObservableBuffer

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}

@Singleton
class BandModeBreakDown @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                        bandModeFactory: BandModeFactory) {

  /**
   * Produces the BandModeBreakDown [[Table]] for the summary HTML page.
   *
   * @param power watts or "kw"
   * @return a [[Table]] that
   */
  def apply(power: String): Table = {
    val powerCell: Cell = Cell(power)
      .withCssClass("sumCell")

    val rows: Seq[Row] = {
      allQsos
        .toSeq
        .groupBy(_.qso.bandMode.bandName)
        .map { bq: (Band, Seq[QsoRecord]) =>
          SumBandRow(bq, powerCell).toRow
        }.toSeq
    }

    Table(Seq(
      Seq(Cell("").withCssClass("cornerCell"), Cell("CW", colSpan = 2), Cell("Digital", colSpan = 2), Cell("Phone", colSpan = 2)),
      Seq("Band", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)")
    ), rows)

  }

  case class SumBandRow(bq: (Band, Seq[QsoRecord]), powerCell: Cell) extends RowSource {
    // Start with entry for each possible mode.
    private val modeCounts: Map[String, AtomicInteger] =
      bandModeFactory.modes.map { availableMode =>
        availableMode.mode -> new AtomicInteger()
      }.toMap

    val (band: Band, qsos: Seq[QsoRecord]) = bq

    qsos.foreach { qsoRecord =>
      val mode = qsoRecord.qso.bandMode.modeName
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