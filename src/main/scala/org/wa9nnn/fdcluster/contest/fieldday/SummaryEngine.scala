
package org.wa9nnn.fdcluster.contest.fieldday

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{AllContestRules, EntryCategories, EntryCategory, QsoRecord}
import play.twirl.api.HtmlFormat
import scalafx.collections.ObservableBuffer

import java.io.Writer
import javax.inject.{Inject, Singleton}

@Singleton
//@Named("allQsos") allQsos: ObservableBuffer[QsoRecord]
class SummaryEngine @Inject()(allContestRules: AllContestRules) {
  def apply(writer: Writer, contest: Contest, wfd:WinterFieldDaySettings): Unit = {
    val contestRules = allContestRules.byContestName(contest.eventName)
    val categories: Seq[EntryCategory] = contestRules.categories.categories.toSeq



    val appendable: HtmlFormat.Appendable = html.FieldDaySummary(contest, wfd, categories)

    val contentType = appendable.contentType
    val str = appendable.toString()
    writer.write(str)
  }
}
