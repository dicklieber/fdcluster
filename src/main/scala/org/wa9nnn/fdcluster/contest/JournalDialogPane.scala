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

import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.javafx.GridOfControls
import _root_.scalafx.scene.control.{Button, Hyperlink, Label, TitledPane}
import _root_.scalafx.scene.layout.VBox
import _root_.scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets
import org.wa9nnn.fdcluster.contest.JournalManager.notSet

import java.awt.Desktop
import javax.inject.Inject
import com.wa9nnn.util.TimeConverters.local
import org.wa9nnn.fdcluster.model.ContestProperty

/**
 * Allow user to create a new Journal file.
 * Journals are name based on [[org.wa9nnn.fdcluster.contest.Journal]] object
 *
 * @param journalProperty manages the [[Journal]]
 * @param fileContext     so we can make link to the journals directory.
 */
class JournalDialogPane @Inject()(journalProperty: JournalManager, fileContext: FileContext, contestProperty: ContestProperty) {
  val gridOfControls = new GridOfControls()
  private val desktop = Desktop.getDesktop

  import org.wa9nnn.fdcluster.contest.JournalManager.tp2String

  private val currentFile: StringProperty = gridOfControls.add("Current", "--")
  private val newJournalButton = new Button("New Journal")
  gridOfControls.add(newJournalButton,
    1, gridOfControls.row.getAndIncrement())

  val lastGoc = new GridOfControls(5 -> 5, Insets(5.0))
  val maybeJournal: Option[Journal] = journalProperty._currentJournal

  val lastFrom: StringProperty = lastGoc.add("From", maybeJournal.map(_.nodeAddress.display).getOrElse(notSet))
  val lastAt: StringProperty = lastGoc.add("At", maybeJournal.map(_.stamp).getOrElse(notSet))
  updateLast() // starting values.
  gridOfControls.add("Last Changed", lastGoc)

  gridOfControls.addControl("Journal Files", new Hyperlink(fileContext.journalDir.toString) {
    tooltip = "A new file will not exist until a QSO is written to it."
    onAction = event => {
      desktop.open(fileContext.journalDir.toFile)
    }
  })

  def updateLast(): Unit = {
    journalProperty._currentJournal match {
      case Some(journal) =>
        currentFile.value = journal.journalFileName
        lastFrom.value = journal.nodeAddress.display
        lastAt.value = journal.stamp
      case None =>
        currentFile.value = "--"
        lastFrom.value = "--"
        lastAt.value = "--"
    }
  }

  journalProperty.journalFilePathProperty.onChange { (_, _, _) =>
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
    contestProperty.okToLogProperty.onChange { (_, _, nv) =>
      pane.visible = nv
    }
  }


}
