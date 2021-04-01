package org.wa9nnn.fdcluster.station

import org.specs2.execute.{AsResult, Result}
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model.{EntryCategory, Exchange}
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory

import scala.util.Try

trait StationDialogLogicContext extends ForEach[StationDialogLogic] {
  def foreach[R: AsResult](r: StationDialogLogic => R): Result = {

    val callSign: StringProperty = new StringProperty("WA9NNN")
    val transmitters = new IntegerSpinnerValueFactory(1, 30, 1)
    val category = new ObjectProperty[EntryCategory]
    val section = new ObjectProperty[Section]
    val exchangeDisplay = new StringProperty()
    val saveButton = new BooleanProperty()
    val stationDialogLogic = new StationDialogLogic(
      callSign,
      transmitters,
      category,
      section,
      new StringProperty(""),
      new StringProperty(""),
      saveButton
    )

     AsResult(r(stationDialogLogic))
  }
}

class StationDialogLogicSpec extends Specification with DataTables with StationDialogLogicContext {
  "StationDialogLogicSpec" should {
    "initial" >> { stationDialogLogic: StationDialogLogic =>
      val exchange: Try[Exchange] = stationDialogLogic.exchange
      exchange must beFailedTry[Exchange]

//      stationDialogLogic.exchangeLabel.value must beEmpty
      stationDialogLogic.saveButtonDisable.value must beTrue
    }
  /*  "ok exchange and callSign" >> { stationDialogLogic: StationDialogLogic =>
      stationDialogLogic.callsign.value = "WA9NNN"
      stationDialogLogic.transmitters.value = 3
      stationDialogLogic.category.value = EntryCategory.observableEntryCategories(1)
      stationDialogLogic.section.value = Sections.byCode("IL")

      stationDialogLogic.exchangeLabel.value must beEqualTo ("3I IL")
      stationDialogLogic.saveButtonDisable.value must beFalse
    }
    "ok exchange with bad callSign" >> { stationDialogLogic: StationDialogLogic =>
      stationDialogLogic.transmitters.value = 3
      stationDialogLogic.category.value = EntryCategory.observableEntryCategories(1)
      stationDialogLogic.section.value = Sections.byCode("IL")

      stationDialogLogic.exchangeLabel.value must beEqualTo ("3I IL")
      stationDialogLogic.saveButtonDisable.value must beFalse
      stationDialogLogic.callsign.value = "W"
      stationDialogLogic.saveButtonDisable.value must beTrue
    }
*/
  }
}
