
package org.wa9nnn.fdcluster.contest.fieldday

import com.wa9nnn.util.tableui.Table
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, EntryCategory}
import play.twirl.api.HtmlFormat

import java.io.{StringWriter, Writer}
import javax.inject.{Inject, Singleton}
import org.wa9nnn.fdcluster.{FileContext, html}

import java.awt.Desktop
import java.nio.file.Files

@Singleton
class SummaryEngine @Inject()(allContestRules: AllContestRules,
                              contestProperty: ContestProperty,
                              fileContext: FileContext,
                              bandModeBreakDown: BandModeBreakDown) {
  def invoke(): Unit = {
    val writer = new StringWriter

    val wfd = WinterFieldDaySettings()
    apply(writer, contestProperty.contest, wfd)
    writer.close()

    fileContext.defaultExportFile(".html", contestProperty)
    val path = Files.createTempFile("SummaryEngineSpec", ".html")
    Files.writeString(path, writer.toString)
    val uri = path.toUri
    Desktop.getDesktop.browse(uri)
  }

  def apply(writer: Writer, contest: Contest, wfd: WinterFieldDaySettings): Unit = {
    val contestRules = allContestRules.byContestName(contest.contestName)
    val categories: Seq[EntryCategory] = contestRules.categories.categories.toSeq

    val bandModeTable: Table = bandModeBreakDown(wfd.power)

    val appendable: HtmlFormat.Appendable = html.FieldDaySummary(contest, wfd, categories, bandModeTable)

    val contentType = appendable.contentType
    val str = appendable.toString()
    writer.write(str)
  }

}

