
package org.wa9nnn.fdcluster.contest.fieldday

import com.google.inject.name.Named
import com.wa9nnn.util.tableui.{Cell, Table}
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{AllContestRules, EntryCategories, EntryCategory, QsoRecord}
import play.twirl.api.HtmlFormat
import scalafx.collections.ObservableBuffer

import java.io.Writer
import javax.inject.{Inject, Singleton}

@Singleton
//@Named("allQsos") allQsos: ObservableBuffer[QsoRecord]
class SummaryEngine @Inject()(allContestRules: AllContestRules) {
  def apply(writer: Writer, contest: Contest, wfd: WinterFieldDaySettings): Unit = {
    val contestRules = allContestRules.byContestName(contest.eventName)
    val categories: Seq[EntryCategory] = contestRules.categories.categories.toSeq

    val rows = Seq ()
    val bandModeBreakDown = Table(Seq(
      Seq("", Cell("CW", colSpan = 2), Cell("Digital", colSpan = 2), Cell("Phone", colSpan = 2)),
      Seq("Band", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)", "QSOs", "Pwr(W)")
    ), rows)

    val appendable1: HtmlFormat.Appendable = com.wa9nnn.util.tableui.html.renderTable(bandModeBreakDown)
    val bandModeetc = appendable1.toString()
    val appendable: HtmlFormat.Appendable = html.FieldDaySummary(contest, wfd, categories)

    val contentType = appendable.contentType
    val str = appendable.toString()
    writer.write(str + bandModeetc)
  }
}
