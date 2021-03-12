
package org.wa9nnn.fdcluster.station

import org.wa9nnn.fdcluster.javafx.CallsignValidator
import org.wa9nnn.fdcluster.javafx.entry.EntryCategory
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model.Exchange
import scalafx.beans.property._
import scalafx.scene.control.SpinnerValueFactory

import scala.util.{Failure, Try}

/**
 * Handles interaction between [[org.wa9nnn.fdcluster.javafx.menu.StationDialog]] controls
 *
 * Given values for: callsign, transmitters, category & section
 * Set exchangeLabel to Exchange
 * if exchange is ok and callsign is valid, then enable Save button
 *
 * @param callsign                value of callsign control.
 * @param transmitters            val of transmitters spinner.
 * @param category                combobox
 * @param section                 combobox
 * @param exchangeLabel           val so unit tests can easily access.
 * @param saveButtonDisable       val so unit tests can easily access.
 */
class StationDialogLogic(
                          val callsign: StringProperty,
                          val transmitters: SpinnerValueFactory[Integer],
                          val category: ObjectProperty[EntryCategory],
                          val section: ObjectProperty[Section],
                          val exchangeLabel: StringProperty,
                          val saveButtonDisable: BooleanProperty) {

 exchangeLabel.value = ""
  saveButtonDisable.value = true

  var exchange: Try[Exchange] = new Failure[Exchange](new IllegalStateException())
  buildExchange()

  def buildExchange(): Unit = {
    exchange = Try {
      val nTransmitters: Int = transmitters.value.value
      val entryCategory: EntryCategory = category.value
      val clas = entryCategory.buildClass(nTransmitters)
      Exchange(clas, section.value.code)
    }
  }

  def fireChange(): Unit = {
    buildExchange()
    exchange.foreach { exchange =>
      exchangeLabel.value = exchange.display
    }
    saveButtonDisable.value = exchange.isFailure ||  CallsignValidator.valid(callsign.value).isDefined

  }

  callsign.onChange{
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