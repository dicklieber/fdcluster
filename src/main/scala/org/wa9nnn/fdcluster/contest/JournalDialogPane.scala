/*
 * Copyright © 2021 Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdcluster.contest

import _root_.scalafx.Includes._
import _root_.scalafx.scene.control.{Button, Hyperlink, Label, TitledPane}
import _root_.scalafx.scene.layout.VBox
import com.wa9nnn.util.TimeConverters.local
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty}
import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets

import java.awt.Desktop
import javax.inject.Inject

/**
 * Allow user to create a new Journal file.
 *
 * @param journalProperty
 * @param fileContext     so we can make link to the journals directory.
 */
class JournalDialogPane @Inject()(journalProperty: JournalProperty,
                                  fileContext: FileContext,
                                  contestProperty: ContestProperty,
                                  allContestRules: AllContestRules) {
  val gridOfControls = new GridOfControls()
  private val desktop = Desktop.getDesktop

  private val currentFile: StringProperty = gridOfControls.add("Current", "--")
  private val newJournalButton = new Button("New Journal")
  gridOfControls.add(newJournalButton,
    1, gridOfControls.row.getAndIncrement())
  if (allContestRules.inSchedule)
    gridOfControls.add(new Label(s"${contestProperty.contestName} is ongoing, do you really want to restart!") {
      styleClass += "warning"
    }, 1, gridOfControls.row.getAndIncrement()
    )
  val lastGoc = new GridOfControls(5 -> 5, Insets(5.0))


  val lastFrom: StringProperty = lastGoc.add("From", "-")
  val lastAt: StringProperty = lastGoc.add("At", "-")
  updateLast() // starting values.
  gridOfControls.add("Last Changed", lastGoc)

  gridOfControls.addControl("Journal Files", new Hyperlink(fileContext.journalDir.toString) {
    tooltip = "A new file will not exist until a QSO is written to it."
    onAction = _ => {
      desktop.open(fileContext.journalDir.toFile)
    }
  })

  def updateLast(): Unit = {
    val journal = journalProperty.value
    currentFile.value = journal.journalFileName
    lastFrom.value = journal.nodeAddress.display
    lastAt.value = journal.stamp
  }

  journalProperty.onChange {
    (_, _, _) =>
      updateLast()
  }

  newJournalButton.onAction = () => {
    journalProperty.createNewJournal()
    goodLuckPane.visible = true
  }
  val goodLuckPane: Label = new Label("Good luck in the contest!") {
    styleClass += "goodLuckPane"
    visible = false
  }

  val pane: TitledPane = new TitledPane() {
    text = "Journal"
    content = new VBox(new Label(
      """QSOs are stored in a journal on each node. FdCluser synchronizes the journal
        |on each node with others.
        |Changing the name of the journal starts new journals on all nodes.
        |Before the contest starts you can make start new journals to practice; but once
        |the contest starts do not start a new journal!
        |Journals contain JSON data. A header line and one line per QSO. You
        |can viw them """.stripMargin) {
      styleClass += "helpPane"
    }
      , gridOfControls,
      goodLuckPane
    )
    collapsible = false
  }
  {
    pane.visible = contestProperty.okToLogProperty.value
    contestProperty.okToLogProperty.onChange {
      (_, _, nv) =>
        pane.visible = nv
    }
  }


}
