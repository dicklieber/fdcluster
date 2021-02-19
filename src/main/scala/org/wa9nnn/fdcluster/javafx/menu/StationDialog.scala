
package org.wa9nnn.fdcluster.javafx.menu

import org.wa9nnn.fdcluster.javafx.entry.{EntryCategory, Sections}
import org.wa9nnn.fdcluster.javafx.{CallSignField, Section}
import org.wa9nnn.fdcluster.model.{OurStation, OurStationStore}
import org.wa9nnn.fdcluster.station.StationDialogLogic
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.JsonLogging
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.util.StringConverter

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * UI for things that need to be setup for the contest.
 *
 * @param ourStationStore where the data lives.
 */
class StationDialog @Inject()(ourStationStore: OurStationStore) extends Dialog[OurStation] with JsonLogging {
  val dp: DialogPane = dialogPane()

  private val saveButton = new ButtonType("Save", ButtonData.OKDone)
  private val cancelButton = ButtonType.Cancel

  private val current: OurStation = ourStationStore.apply()
  private val callSign = new CallSignField() {
    text = current.ourCallsign
    tooltip = """Used in the "Info Sent" field in QSO."""
  }
  private val transmitters = new Spinner[Integer](1, 30, 1) {
    valueFactory().value = current.transmitters
    tooltip = "Number of simultaneous transmitters. Combined with category to produce exchange class."
  }

  private val category = new ComboBox[EntryCategory](EntryCategory.catagories) {
    tooltip = "Combined with number of transmitters to make exchange class."
    converter = StringConverter.toStringConverter {
      case h: EntryCategory =>
        h.category
      case _ => "-Choose Category-"
    }
    selectionModel.value.select(EntryCategory.fromEntryClass(current.exchange.entryClass))
  }

  private val section = new ComboBox[Section](Sections.sortedByCode) {
    tooltip = "ARRL section for exchange sent."

    converter = StringConverter.toStringConverter { v =>
      if (v != null)
        v.toString
      else
        "-Choose Section-"
    }
    selectionModel.value.select(Sections.byCode(current.exchange.section))
  }

  private val rig = new TextField() {
    text = current.rig
  }
  private val antenna = new TextField() {
    text = current.antenna
  }

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

  title = "Station Configuration"
  headerText = "Configuration for this station"

  // Build the result
  resultConverter = {
    button: ButtonType ⇒
      if (button == saveButton) {
        val newOurStation = OurStation(callSign.text.value, stationDialogLogic.exchange.get, rig.text.value, antenna.text.value)
        ourStationStore.value = newOurStation
      }
      null
  }

  dp.getButtonTypes.addAll(saveButton, cancelButton)
  dp.getStylesheets.addAll(
    getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm,
    getClass.getResource("/fdcluster.css").toExternalForm
  )

  private val grid: GridPane = new GridPane() {
    hgap = 10
    vgap = 10
    padding = Insets(20, 100, 10, 10)
    val row = new AtomicInteger()

    def add(label: String, node: Node): Unit = {
      val r = row.getAndIncrement()
      add(new Label(label + ":"), 0, r)
      add(node, 1, r)
    }

    add("Station Callsign", callSign)
    add("Transmitter", transmitters)
    add("Category", category)
    add("Section", section)
    add("Rig", rig)
    add("Antenna", antenna)

  }
  val stationDialogLogic = new StationDialogLogic(
    callSign.text,
    transmitters.valueFactory.value,
    category.value,
    section.value,
    exchangeDisplay.text,
    dp.lookupButton(saveButton).disableProperty()
  )

  grid.add(exchangePane, 2, 1, 1, 3)

  val c0 = new ColumnConstraints()
  c0.setPercentWidth(33)

  grid.columnConstraints.addAll(c0, c0, c0
  )

  dialogPane().setContent(grid)
  forceCaps(callSign)
  Platform.runLater(callSign.requestFocus())
}

