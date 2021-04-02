
package org.wa9nnn.fdcluster.contest.fieldday

import com.google.inject.name.Named
import com.wa9nnn.util.tableui.{Cell, Row, RowSource, Table}
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.CurrentStation.Band
import org.wa9nnn.fdcluster.model.{AllContestRules, BandModeFactory, EntryCategories, EntryCategory, QsoRecord}
import play.twirl.api.HtmlFormat
import scalafx.collections.ObservableBuffer

import java.io.Writer
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable

@Singleton
class SummaryEngine @Inject()(allContestRules: AllContestRules,
                              @Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                              bandModeFactory: BandModeFactory) {
  def apply(writer: Writer, contest: Contest, wfd: WinterFieldDaySettings): Unit = {
    val contestRules = allContestRules.byContestName(contest.eventName)
    val categories: Seq[EntryCategory] = contestRules.categories.categories.toSeq
    val powerCell: Cell = Cell(wfd.power)
      .withCssClass("sumCell")

    val rows: Seq[Row] = {
      allQsos
        .toSeq
        .groupBy(_.qso.bandMode.bandName)
        .map { bq: (Band, Seq[QsoRecord]) =>
          SumBandRow(bq, powerCell).toRow
        }.toSeq
    }

    val bandModeBreakDown = Table(Seq(
      Seq(Cell("").withCssClass("cornerCell"), Cell("CW", colSpan = 2), Cell("Digital", colSpan = 2), Cell("Phone", colSpan = 2)),
      Seq("Band", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)")
    ), rows)


    val appendable1: HtmlFormat.Appendable = com.wa9nnn.util.tableui.html.renderTable(bandModeBreakDown)
    val bandModeetc = appendable1.toString()
    val appendable: HtmlFormat.Appendable = html.FieldDaySummary(contest, wfd, categories)

    val contentType = appendable.contentType
    val str = appendable.toString()
    writer.write(str + bandModeetc)
  }


  case class SumBandRow(bq: (Band, Seq[QsoRecord]), powerCell: Cell) extends RowSource {
    // Start with entry for each possible mode.
    private val modeCounts: Map[String, AtomicInteger] =
      bandModeFactory.modes.map { availableMode =>
        availableMode.mode -> new AtomicInteger()
      }.toMap

    val (band: Band, qsos: Seq[QsoRecord]) = bq

    qsos.foreach{qsoRecord =>
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

