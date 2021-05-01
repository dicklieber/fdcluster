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

import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.CallSign.s2cs
import org.wa9nnn.fdcluster.station.StationDialogLogic
import _root_.scalafx.Includes._
import _root_.scalafx.beans.property.{ObjectProperty, StringProperty}
import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.geometry.{Insets, Pos}
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.VBox

import javax.inject.Inject

case class ContestDialogPane @Inject()(contestProperty: ContestProperty,
                             contestRules: AllContestRules,
                             nodeAddress: NodeAddress)   {
  private val gridOfControls = new GridOfControls
  private val saveButton = new Button("Save to Cluster")

  private val callSignProperty: StringProperty = gridOfControls.addText("CallSign",
    tooltip = Some("""CallSign of the club or individual entrant."""),
    defValue = contestProperty.callSign,
    forceCaps = true)

  private val contestCB: ObjectProperty[String] = gridOfControls.addCombo[String](
    labelText = "Contest",
    choices = ObservableBuffer.from(contestRules.contestNames),
    defValue = Some(contestProperty.contestName)
  )

  private val currentExchange: Exchange = contestProperty.ourExchange

  private val transmitters = new {
  } with Spinner[Integer](1, 30, currentExchange.transmitters) {
    valueFactory().value = currentExchange.transmitters
    tooltip = "Number of simultaneous transmitters. Combined with category to produce exchange class."
  }
  gridOfControls.addControl("Transmitters", transmitters)

  val categoryCB: ComboBox[EntryCategory] = new ComboBox[EntryCategory]()
  gridOfControls.addControl("Category", categoryCB)

  contestCB.onChange { (_, _, contestName: String) =>
    setup(contestName)
  }
  setup(contestProperty.contestName)

  private def setup(contestName: String): Unit = {
    //    contestProperty.contestNameProperty.value = contestName
    val rules = contestRules.byContestName(contestName)
    categoryCB.items = rules.categories.categories
    categoryCB.value = {
      rules.categories.entryCategoryForDesignator(currentExchange.category)
    }
  }

  val sectionProperty: ObjectProperty[Section] = gridOfControls.addCombo[Section](
    labelText = "Section",
    defValue = Some(Sections.byCode(currentExchange.sectionCode)),
    tooltip = Option("ARRL sectionCode for exchange sent."),
    choices = Sections.forChoice())

  val exchangeText: Label = new Label()
  val exchangeMnemonics: Label = new Label() {
    styleClass += "exchangeMnemonics"
  }
  private val exchangeDisplay: VBox = new VBox(
    exchangeText, exchangeMnemonics
  )
  private val exchangePane = new VBox(
    new Label("Exchange:"),
    exchangeDisplay
  ) {
    alignmentInParent = Pos.Center
    styleClass += "exchangeDisplay"
    styleClass += "exchangeBlock"
  }

  val stationDialogLogic = new StationDialogLogic(
    callSignProperty,
    transmitters.valueFactory.value,
    categoryCB.value,
    sectionProperty,
    exchangeMnemonics.text,
    exchangeText.text,
    saveButton.disableProperty()
  )
  saveButton.onAction = () =>
    stationDialogLogic.exchange.foreach {
      exchange =>
        val newContest = Contest(callSign = callSignProperty.value,
          ourExchange = exchange,
          contestName = contestCB.value,
          nodeAddress = nodeAddress)

        contestProperty.save(newContest)
    }

  gridOfControls.add("Exchange", exchangePane)
  private val contest: Contest = contestProperty.contest

  val lastGoc = new GridOfControls(5 -> 5, Insets(5.0))
  lastGoc.add("From", contest.nodeAddress.display)
  lastGoc.add("At", contest.stamp)
  gridOfControls.add("Last Changed", lastGoc)

  gridOfControls.add(saveButton,
    1, gridOfControls.row.getAndIncrement())

  val pane: TitledPane = new TitledPane() {
    text = "Contest Settings"
    content = new VBox(new Label(
      """These should be set before the contest begins.
        |Changes will be propagated to all nodes in the cluster including new
        |or rejoined members.
        |Changing the Exchange after the contest begins will result in
        |busted QSOs!
        |CallSign is not used until post contest. Changing during the contest is ok.
        |""".stripMargin) {
      styleClass += "helpPane"
    }
      , gridOfControls)
    collapsible = false
  }
}
