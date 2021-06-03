
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

package org.wa9nnn.fdcluster.rig

import _root_.scalafx.Includes._
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.scene.control.ComboBox.sfxComboBox2jfx
import _root_.scalafx.scene.control._
import _root_.scalafx.util.StringConverter
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.rig.SerialPortSettings.baudRates
import scalafx.collections.ObservableBuffer
import scalafx.scene.image
import scalafx.scene.image.Image
import scalafx.scene.input.{Clipboard, ClipboardContent, DataFormat}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

import java.awt.Desktop
import java.net.URI
import javax.inject.Inject
import scala.util.{Failure, Success, Using}

class RigDialog @Inject()(rigStore: RigStore, rigList: Rigctld, config: Config) extends Dialog[RigSettings] with LazyLogging {

  val initRigSettings: RigSettings = rigStore.rigSettings.value

  val initRigModel: RigModel = initRigSettings.rigModel
  private val mfgSelect = new ComboBox[String](ObservableBuffer.from(rigList.rigManufacturers)) {
    value = initRigModel.mfg
    tooltip = "Choose manufacturer of your radio, this will populate models."
  }
  private val modelSelect = new ComboBox[RigModel] {
    value = initRigModel
    tooltip = "Choose your radio."
    cellFactory = { _ =>
      new ListCell[RigModel]() {
        item.onChange { (_, oldValue, newValue) => {
          val choice = Option(newValue).getOrElse(oldValue).choice
          text = choice
        }
        }
      }
    }
    converter = StringConverter.toStringConverter((h: RigModel) => {
      if (h == null)
        "- Choose Rig Model -"
      else {
        h.model
      }
    })
  }
  val currentRigSettings: RigSettings = rigStore.rigSettings.value
  val rigModel: RigModel = currentRigSettings.rigModel
  mfgSelect.setValue(rigModel.mfg)
  modelSelect.setValue(rigModel)
  mfgSelect.onAction = (_: ActionEvent) => {

    val selectedMfg: String = mfgSelect.value.apply()

    val rigModels: Seq[RigModel] = rigList.modelsForMfg(selectedMfg).sorted
    val currentRigModel = modelSelect.value.value
    modelSelect.items = ObservableBuffer.from(rigModels)
    if (rigModels.contains(currentRigModel))
      modelSelect.value = currentRigModel
    else
      modelSelect.value = rigModels.head
  }

  val enableRig: CheckBox = new CheckBox() {
    selected = initRigSettings.enable
  }
  val launchRigctldCheckBox: CheckBox = new CheckBox() {
    selected = initRigSettings.launchRigctld
  }
  val rigctldCmd: TextField = new TextField() {
    text.value = initRigSettings.rigctldCommand
    prefColumnCount = 50
  }
  val setDefaultRigctldCmd: Button = new Button("Default") {
    onAction = _ => {
      rigctldCmd.text.value = config.getString("fdcluster.rig.launchRigctld")
    }
  }

  val rigctldHostPort: TextField = new TextField() {
    text.value = initRigSettings.rigctldHostPort
    prefColumnCount = 20
  }
  val setDefaultRigctldHostPort: Button = new Button("Default") {
    onAction = _ => {
      rigctldHostPort.text.value = "127.0.0.1:4532"
    }
  }

  val portComboBox: ComboBox[SerialPort] = new ComboBox[SerialPort](ObservableBuffer.from(Serial.ports)) {
    converter = StringConverter.toStringConverter((h: SerialPort) => {
      if (h == null)
        "- Choose Serial Port -"
      else {
        h.display
      }
    })


    cellFactory = { _ =>
      new ListCell[SerialPort]() {
        item.onChange { (_, oldValue, newValue) => {
          val choice = Option(newValue).getOrElse(oldValue).display
          text = choice
        }
        }
      }
    }
    placeholder = new ListCell() {
      text = "-choose-"
    }
    initRigSettings.port.foreach { sp =>
      value = sp
    }
  }
  val baudRateComboBox: ComboBox[String] = new ComboBox[String](ObservableBuffer.from(baudRates)) {
    value = initRigSettings.baudRate
  }

  val sampleRigctld: Label = new Label() {
    disable = true
    styleClass += "example"
  }
  val copyRigctldCommandLineButton: Button = new Button() {
    val imagePath: String = s"/images/clipboard-line.png"
    Using(getClass.getResourceAsStream(imagePath)) { is =>
      new Image(is, 20.0, 20.0, true, true)
    } match {
      case Failure(exception) =>
        logger.error(s"loading: $imagePath", exception)
      case Success(i) =>
        graphic.value  = new image.ImageView(i)
    }
    onAction = { _ =>
      Clipboard.systemClipboard.content = ClipboardContent(
        DataFormat.PlainText -> sampleRigctld.text.value
      )
    }
  }


  private val goc = new GridOfControls()
  goc.addControl("Rig Manufacturer", mfgSelect)
  goc.addControl("Rig Model", modelSelect)
  goc.addControl("Serial Port", portComboBox)
  goc.addControl("Baud Rate", baudRateComboBox)
  goc.addControl("Enable", enableRig)

  goc.addControl("rigctld command", rigctldCmd, setDefaultRigctldCmd)
  goc.addControl("rigctld host", rigctldHostPort, setDefaultRigctldHostPort)
  goc.addControl("Launch rigctld", new HBox(launchRigctldCheckBox, sampleRigctld, copyRigctldCommandLineButton))

  private val dp: control.DialogPane = dialogPane()
  dp.getStylesheets.addAll(
    getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm,
    getClass.getResource("/fdcluster.css").toExternalForm
  )

  private val saveButton = new ButtonType("Save")

  new RigctldCommand(modelSelect, baudRateComboBox, portComboBox, rigctldCmd, sampleRigctld)

  resultConverter = { button =>
    if (button == saveButton) {
      val model: RigModel = modelSelect.value.apply()
      val settings = RigSettings(
        rigModel = model,
        port = Option(portComboBox.value.value),
        baudRate = baudRateComboBox.value.value,
        enable = enableRig.selected.value,
        launchRigctld = launchRigctldCheckBox.selected.value,
        rigctldHostPort = rigctldHostPort.text.value,
        rigctldCommand = rigctldCmd.text.value
      )
      rigStore.rigSettings.value = settings
      settings
    } else {
      logger.trace("No change to rig")
      null //todo
    }
  }
  val desktop: Desktop = Desktop.getDesktop

  private val borderPane = new BorderPane() {
    top = new VBox(new Label(
      """main uses rigctld application from Hamlib to list for frequency and mode from your radio.
        |rigctld is a application that is configured to talk to specific radios, FdCuster talks to an instance of
        |rigctld over IP.
        |You need to install Hamlib for your particular computer. See the Hamlib documentation link below.
        |FCLuster can automatically start and stop rigctld when the application starts and stops or you can
        |start rigctld manually with what ever command line you wish.
        |""".stripMargin) {
      styleClass += "helpPane"
    },
      new HBox(
        new Hyperlink("hamlib documentation") {
          onAction = _ => {
            desktop.browse(new URI("https://hamlib.github.io"))
          }
        },
        new Hyperlink("rigctld documentation") {
          onAction = _ => {
            desktop.browse(new URI("https://manpages.ubuntu.com/manpages/trusty/man8/rigctld.8.html"))
          }
        }
      )
    )
    center = goc
  }
  dp.setContent(borderPane)
  private val buttonTypes: ObservableList[control.ButtonType] = dp.getButtonTypes
  buttonTypes.add(ButtonType.Close)
  buttonTypes.add(saveButton)

  override def onCloseRequest_=(v: EventHandler[control.DialogEvent]): Unit = {
    super.onCloseRequest_=(v)
  }
}
