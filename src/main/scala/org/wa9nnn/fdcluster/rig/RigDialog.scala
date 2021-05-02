
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
import _root_.scalafx.scene.layout.{BorderPane, HBox}
import _root_.scalafx.util.StringConverter
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control
import org.wa9nnn.util.StructuredLogging
import scalafx.collections.ObservableBuffer

import javax.inject.Inject

class   RigDialog @Inject()(rigStore: RigStore) extends Dialog[RigSettings] with StructuredLogging{
  private val riglist = new RigList()

  //  def apply(): Unit = {
val initRigModel: RigModel = rigStore.rigSettings.value.rigModel
  private val mfgSelect = new ComboBox[String](ObservableBuffer.from(riglist.mfgs)){
    value = initRigModel.mfg
  }
  private val modelSelect = new ComboBox[RigModel] {
    value = initRigModel
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
  private val catControlPanel: CatControlPanel = new CatControlPanel() {
    val currentRigSettings: RigSettings = rigStore.rigSettings.value
    val rigModel = currentRigSettings.rigModel
    mfgSelect.setValue(rigModel.mfg)
    modelSelect.setValue(rigModel)
    setValue(currentRigSettings.serialPortSettings)
  }
  mfgSelect.onAction = (_: ActionEvent) => {

    val selectedMfg: String = mfgSelect.value.apply()

    val rigModels: Seq[RigModel] = riglist.modelsForMfg(selectedMfg).sorted
    modelSelect.items = ObservableBuffer.from(rigModels)
  }
  val borderPane: BorderPane = new BorderPane {
    top = new HBox(
      new Label("Manufacture:"),
      mfgSelect,
      //        Space,
      new Label("Model:"),
      modelSelect,
    )
    center = catControlPanel
  }
  private val pane: control.DialogPane = dialogPane()
  private val saveButton = new ButtonType("Save")

  resultConverter = { button =>
    if( button ==  saveButton ) {
      val model: RigModel = modelSelect.value.apply()
      val serialPort: SerialPortSettings = catControlPanel.result
      //todo handle not set Option
      val settings = RigSettings(model, serialPort)
      rigStore.rigSettings.value = settings
      settings
    }else {
      logger.trace("No change to rig")
      null //todo
    }
  }

  pane.setContent(borderPane)
  private val buttonTypes: ObservableList[control.ButtonType] = pane.getButtonTypes
  buttonTypes.add(ButtonType.Close)
  buttonTypes.add(saveButton)


  override def onCloseRequest_=(v: EventHandler[control.DialogEvent]): Unit = {
    super.onCloseRequest_=(v)
  }


}
