
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

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.{Node, control}
import org.wa9nnn.util.StructuredLogging
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.ComboBox.sfxComboBox2jfx
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.util.StringConverter

import javax.inject.Inject

class RigDialog @Inject()(rigStore: RigStore) extends Dialog[RigSettings] with StructuredLogging{
  println("org.wa9nnn.fdcluster.rig.RigDialog")
  private val riglist = new RigList()

  //  def apply(): Unit = {

  private val mfgSelect = new ComboBox[String](ObservableBuffer[String](riglist.mfgs))
  private val modelSelect = new ComboBox[RigModel] {
    cellFactory = { x =>
      new ListCell[RigModel]() {
        item.onChange { (ov, oldValue, newValue) => {
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
  private val catControlPanel = new CatControlPanel() {
    val currentRigSettings: RigSettings = rigStore.rigSettings.value
    val rigModel = currentRigSettings.rigModel
    mfgSelect.setValue(rigModel.mfg)
    modelSelect.setValue(rigModel)
    setValue(currentRigSettings.serialPortSettings)
  }
  mfgSelect.onAction = (e: ActionEvent) => {

    val selectedMfg: String = mfgSelect.value.apply()

    val rigModels: Seq[RigModel] = riglist.modelsForMfg(selectedMfg)
    modelSelect.items = ObservableBuffer[RigModel](rigModels)
  }
  val borderPane = new BorderPane {
    top = new HBox(
      new Label("Manufacture:"),
      mfgSelect,
      //        Space,
      new Label("Model:"),
      modelSelect,
    )
    center = catControlPanel
  }

  resultConverter = {
    case saveButton =>
      val model: RigModel = modelSelect.value.apply()
      val serialPort: SerialPortSettings = catControlPanel.result
      //todo handle not set Option
      val settings = RigSettings(model, serialPort)
      rigStore.rigSettings.value = settings
      settings
    case _ =>
      logger.trace("No change to rig")
      null //todo
  }
  private val pane: control.DialogPane = dialogPane()
  private val saveButton = new ButtonType("Save")

  pane.setContent(borderPane)
  private val buttonTypes: ObservableList[control.ButtonType] = pane.getButtonTypes
  buttonTypes.add(ButtonType.Close)
  buttonTypes.add(saveButton)


  override def onCloseRequest_=(v: EventHandler[control.DialogEvent]): Unit = {
    super.onCloseRequest_=(v)
  }


}
