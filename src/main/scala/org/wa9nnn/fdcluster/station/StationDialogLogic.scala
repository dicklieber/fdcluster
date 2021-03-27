
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

package org.wa9nnn.fdcluster.station

import org.wa9nnn.fdcluster.javafx.CallsignValidator
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model.{EntryCategory, Exchange, FdClass}
import org.wa9nnn.util.Mnomonics
import scalafx.beans.property._
import scalafx.scene.control.SpinnerValueFactory

import scala.util.{Failure, Try}

/**
 * Handles interaction between [[org.wa9nnn.fdcluster.javafx.menu.ContestDialog]] controls
 *
 * Given values for: callSign, transmitters, category & sectionCode
 * Set exchangeLabel to Exchange
 * if exchange is ok and callSign is valid, then enable Save button
 *
 * @param callsign          value of callSign control.
 * @param transmitters      val of transmitters spinner.
 * @param category          combobox
 * @param section           combobox
 * @param exchangeLabel     val so unit tests can easily access.
 * @param saveButtonDisable val so unit tests can easily access.
 */
class StationDialogLogic(
                          val callsign: StringProperty,
                          val transmitters: SpinnerValueFactory[Integer],
                          val category: ObjectProperty[EntryCategory],
                          val section: ObjectProperty[Section],
                          val exchangeTextProperty: StringProperty,
                          val exchangeMnemonicsProperty: StringProperty,
                          val saveButtonDisable: BooleanProperty) {

  exchangeMnemonicsProperty.value = ""
  exchangeTextProperty.value = ""
  saveButtonDisable.value = true

  var exchange: Try[Exchange] = new Failure[Exchange](new IllegalStateException())
  buildExchange()

  def buildExchange(): Unit = {
    exchange = Try {
      val nTransmitters: Int = transmitters.value.value
      val entryCategory: EntryCategory = category.value
      Exchange(nTransmitters, entryCategory, section.value)
    }
  }

  def fireChange(): Unit = {
    buildExchange()
    exchange.foreach { exchange =>
      exchangeMnemonicsProperty.value = exchange.display
      exchangeTextProperty.value =  Mnomonics(exchange.display)
    }
    saveButtonDisable.value = exchange.isFailure || CallsignValidator.valid(callsign.value).isDefined

  }

  callsign.onChange {
    fireChange()
  }

  transmitters.value.onChange {
    fireChange()
  }
  category.onChange {
    fireChange()
  }
  section.onChange {
    fireChange()
  }

  fireChange()
}