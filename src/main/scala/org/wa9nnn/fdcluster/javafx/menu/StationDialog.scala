
package org.wa9nnn.fdcluster.javafx.menu

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.wa9nnn.fdcluster.model.BandMode.{Band, Mode}
import org.wa9nnn.fdcluster.model.{BandMode, BandModeFactory, CurrentStation, CurrentStationProvider, OurStation}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout.GridPane


class StationDialog @Inject()(currentStationProvider: CurrentStationProvider, bandModeFactory: BandModeFactory) extends LazyLogging {
  private val saveButton = new ButtonType("Save", ButtonData.OKDone)
  private val cancelButton = ButtonType.Cancel

  def apply(): Unit = {
    val currentStation: CurrentStation = currentStationProvider.currentStation
    val operator = new TextField() {
      promptText = "Operator's Callsign"
      text = currentStation.ourStation.operator
    }
    val rig = new TextField() {
      promptText = "Rig"
      text = currentStation.ourStation.rig
    }
    val antenna = new TextField() {
      promptText = "Antenna"
      text = currentStation.ourStation.antenna
    }
    val band = new ComboBox[Band](bandModeFactory.bands) {
    }
    val bandSelectionModel = band.getSelectionModel
    bandSelectionModel.select(currentStation.bandMode.band)

    val mode = new ComboBox[Mode](bandModeFactory.modes)
    val modeSelectionModel = mode.getSelectionModel
    val currentMode = currentStation.bandMode.mode
    val mode1 = Option(currentMode).getOrElse(bandModeFactory.modes.head)
    modeSelectionModel.select(mode1)

    val dialog = new Dialog[CurrentStation]() {
      //      initOwner(stage)
      title = "Station"
      headerText = "Configuration for this station"
      resultConverter = { f ⇒
        if (f == saveButton) {
          CurrentStation(
            OurStation(operator.text.value, rig.text.value, antenna.text.value),
            BandMode(band.value.value, mode.value.value)
          )
        } else
          null
      }
    }
    dialog.dialogPane().getButtonTypes.addAll(saveButton, cancelButton)

    val grid = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("Operator Callsign:"), 0, 0)
      add(operator, 1, 0)

      add(new Label("Band:"), 0, 1)
      add(band, 1, 1)

      add(new Label("Mode:"), 0, 2)
      add(mode, 1, 2)

      add(new Label("Rig:"), 0, 3)
      add(rig, 1, 3)

      add(new Label("Antenna:"), 0, 4)
      add(antenna, 1, 4)
    }
    dialog.dialogPane().setContent(grid)

    // Request focus on the username field by default.
    Platform.runLater(operator.requestFocus())

    dialog.showAndWait().asInstanceOf[Option[CurrentStation]].foreach { cs ⇒
      currentStationProvider.update(cs)
    }
  }

}
