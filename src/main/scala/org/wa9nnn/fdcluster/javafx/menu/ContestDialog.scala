
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
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
 *
 */

package org.wa9nnn.fdcluster.javafx.menu

import javafx.beans.property.ReadOnlyObjectProperty
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.javafx.entry.{EntryCategory, Sections}
import org.wa9nnn.fdcluster.model.{Contest, ContestProperty, Exchange}
import org.wa9nnn.fdcluster.station.StationDialogLogic
import org.wa9nnn.util.StructuredLogging
import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.geometry.Pos
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.util.StringConverter

import javax.inject.Inject

/**
 * UI for things that need to be setup for the contest.
 *
 * @param contest where the data lives.
 */
class ContestDialog @Inject()(contestProperty: ContestProperty) extends Dialog[Contest] with StructuredLogging {
  val dp: DialogPane = dialogPane()

  private val saveButton = new ButtonType("Save", ButtonData.OKDone)
  private val cancelButton = ButtonType.Cancel


  private val gridOfControls = new GridOfControls
  private val callSignProperty: StringProperty = gridOfControls.addText("CallSign",
    tooltip = Some("""Callsign of the club or individual entrant."""),
    forceCaps = true)
  callSignProperty <==> contestProperty.callSignProperty

  private val eventProperty: ObjectProperty[String] = gridOfControls.addCombo[String](
    labelText = "Contest",
    choices = Seq("FD", "WFD"),
    tooltip = Option(
      """FD ARRL Field Day (June).
        |WFD Winter Field Day (January)""".stripMargin)
  )
  private val stringStringConverter: StringConverter[String] = new StringConverter[String] {
    override def fromString(string: String) = string

    override def toString(t: String) = t
  }
  eventProperty <==> contestProperty.eventProperty

  val yearProperty: StringProperty = gridOfControls.addText(labelText = "Year",
    regx = Some("""\d""".r)
  )
  yearProperty <==> contestProperty.eventYearProperty

  private val currentExchange: Exchange = contestProperty.ourExchange
  private val transmitters = new {
  } with Spinner[Integer](1, 30, currentExchange.transmitters) {
    valueFactory().value = currentExchange.transmitters
    tooltip = "Number of simultaneous transmitters. Combined with category to produce exchange class."
  }
  gridOfControls.add("Transmitters", transmitters)

  val categoryProperty: ObjectProperty[EntryCategory] = gridOfControls.addCombo[EntryCategory](
    labelText = "Category",
    choices = EntryCategory.categories,
    converter = Option(
      StringConverter.toStringConverter {
        case h: EntryCategory =>
          h.category
        case _ => "-Choose Category-"
      }
    )
  )
  categoryProperty.value = currentExchange.category

  val sectionProperty: ObjectProperty[Section] = gridOfControls.addCombo[Section](
    labelText = "Section",
    tooltip = Option("ARRL section for exchange sent."),
    choices = Sections.sortedByCode)

  sectionProperty.value = Sections.byCode( currentExchange.section)

  private val exchangeDisplay: Label = new Label() {
    style = ""
  }
  private val exchangePane = new VBox(
    new Label("Exchange:"),
    exchangeDisplay
  ) {
    alignmentInParent = Pos.Center
    styleClass += "exchangeDisplay"
    styleClass += "exchangeBlock"
  }


  title = "Station Settings"
  headerText = "Contest settings for this station"

  // Build the result
  resultConverter = {
    button: ButtonType ⇒
      if (button == saveButton) {
        stationDialogLogic.exchange.foreach { exchange =>
          contestProperty.ourExchangeProperty.value = exchange
          contestProperty.save()
        }
      }
      null
  }

  dp.getButtonTypes.addAll(saveButton, cancelButton)
  dp.getStylesheets.addAll(
    getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm,
    getClass.getResource("/fdcluster.css").toExternalForm
  )

  val stationDialogLogic = new StationDialogLogic(
    callSignProperty,
    transmitters.valueFactory.value,
    categoryProperty,
    sectionProperty,
    exchangeDisplay.text,
    dp.lookupButton(saveButton).disableProperty()
  )

  gridOfControls.add(exchangePane, 2, 3, 1, 3)
  dialogPane().setContent(gridOfControls)
}

