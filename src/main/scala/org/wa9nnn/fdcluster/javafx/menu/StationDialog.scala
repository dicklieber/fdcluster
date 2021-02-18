
package org.wa9nnn.fdcluster.javafx.menu

import javafx.scene.control
import org.wa9nnn.fdcluster.javafx.{Section, ValidatedText}
import org.wa9nnn.fdcluster.javafx.entry.{EntryCategory, Sections}
import org.wa9nnn.fdcluster.model.{Exchange, OurStation, OurStationStore}
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.JsonLogging
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{IntegerProperty, ObjectProperty, ReadOnlyObjectProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.{Border, BorderPane, ColumnConstraints, GridPane, Pane, Region, VBox}
import scalafx.util.StringConverter

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * UI for things that need to be setup for the contest.
 *
 * @param ourStationStore where the data lives.
 */
class StationDialog @Inject()(ourStationStore: OurStationStore) extends Dialog[OurStation] with JsonLogging {
  private val saveButton = new ButtonType("Save", ButtonData.OKDone)
  private val cancelButton = ButtonType.Cancel

  private val current: OurStation = ourStationStore.apply()
  private val callSign = new TextField() {
    text = current.ourCallsign
  }
  private val transmitters = new Spinner[Int](1, 30, 1) {
    valueFactory().value = current.transmitters
  }

  private val category = new ComboBox[EntryCategory](EntryCategory.catagories) {
    converter = StringConverter.toStringConverter {
      case h: EntryCategory =>
        h.category
      case _ => ""
    }
    current.category.foreach {
      ec =>
        selectionModel.value.select(ec)
    }
  }

  private val section = new ComboBox[Section](Sections.sortedByCode) {
    converter = StringConverter.toStringConverter(_.toString)
    selectionModel.value.select(Sections.byCode(current.exchange.section))
  }
  private val rig = new TextField() {
    text = current.rig
  }
  private val antenna = new TextField() {
    text = current.antenna
  }

  /**
   *
   * @param saveButton to be enabled if no errors.
   * @param validated  controls to be validated.
   */
  def enableValidation(saveButton: Node, validated: ValidatedText*): Unit = {
    saveButton.disable = true
    validated.foreach {
      vt: ValidatedText =>
        vt.errLabel.text = ""
        vt.errLabel.styleClass += "sad"
        vt.onKeyTyped = {
          (_: KeyEvent) =>
            val errors = validated.flatMap(_.validate())
            if (errors.isEmpty) {
              vt.errLabel.text = ""
              saveButton.disable = false
            } else {
              vt.errLabel.text = errors.mkString("\n")
              saveButton.disable = true
            }
        }
    }
  }

  title = "Station Configuration"
  headerText = "Configuration for this station"

  // Build the result
  resultConverter = {
    button: ButtonType ⇒
      if (button == saveButton) {
        val nTransmitters = transmitters.value.apply()
        val entryCategory: EntryCategory = category.value.value
        val cat = f"$nTransmitters${
          entryCategory.designator
        }"
        val exchange = Exchange(cat, section.value.value.code)
        val newOurStation = OurStation(callSign.text.value, exchange, rig.text.value, antenna.text.value)
        ourStationStore.value = newOurStation

      }
      null
  }

  val dp: control.DialogPane = dialogPane()
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

    private val exchangeDisplay = new ExchangeDisplay(transmitters.value, category.value, section.value)
    add("Station Callsign", callSign)
    add("Transmitter", transmitters)
    add("Category", category)
    add("Section", section)
    add("Rig", rig)
    add("Antenna", antenna)

    add(exchangeDisplay(), 2, 1, 1, 3)
  }
  val c0 = new ColumnConstraints()
  c0.setPercentWidth(33)

  grid.columnConstraints.addAll(c0, c0, c0
  )

  dialogPane().setContent(grid)

  //  enableValidation(dialogPane().lookupButton(saveButton), entryClass, section)
  forceCaps(callSign)
  //    forceCaps(callSign, entryClass, section)
  //  forceInt(transmitters)
  // Request focus on
  Platform.runLater(callSign.requestFocus())
}

class ExchangeDisplay(transmitters: ReadOnlyObjectProperty[Int], category: ObjectProperty[EntryCategory], section: ObjectProperty[Section]) {
  private val exchangeResult = new Label() {
    style = ""
  }
  showExchange()
  private val box = new VBox(
    new Label("Exchange:"),
    exchangeResult
  ) {
    alignmentInParent = Pos.Center
    styleClass += "exchangeDisplay"
    styleClass += "exchangeBlock"

  }

  private def showExchange(): Unit = {
    exchangeResult.text = s"${transmitters.value}${category.value.designator} ${section.value.code}"
  }

  transmitters.onChange {
    showExchange()
  }
  category.onChange {
    showExchange()
  }
  section.onChange {
    showExchange()
  }

  def apply(): Pane = new BorderPane() {
    center = box
//    styleClass += "exchangeDisplay"
  }
}