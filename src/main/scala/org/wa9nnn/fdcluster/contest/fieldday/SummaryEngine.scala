
package org.wa9nnn.fdcluster.contest.fieldday

import com.wa9nnn.util.tableui.Table
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{AllContestRules, EntryCategory}
import play.twirl.api.HtmlFormat

import java.io.Writer
import javax.inject.{Inject, Singleton}

@Singleton
class SummaryEngine @Inject()(allContestRules: AllContestRules,
                              bandModeBreakDown: BandModeBreakDown) {
  def apply(writer: Writer, contest: Contest, wfd: WinterFieldDaySettings): Unit = {
    val contestRules = allContestRules.byContestName(contest.eventName)
    val categories: Seq[EntryCategory] = contestRules.categories.categories.toSeq

    val bandModeTable: Table = bandModeBreakDown(wfd.power)

    val appendable: HtmlFormat.Appendable = html.FieldDaySummary(contest, wfd, categories, bandModeTable)

    val contentType = appendable.contentType
    val str = appendable.toString()
    writer.write(str)
  }

}

