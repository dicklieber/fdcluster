
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

package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.Includes._
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.scene.control.{ComboBox, Control, Label, TextField}
import _root_.scalafx.scene.layout.GridPane
import javafx.collections.ObservableList
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestRules, StationProperty, KnownOperatorsProperty}
import org.wa9nnn.fdcluster.rig.RigInfo
import org.wa9nnn.util.InputHelper.forceCaps
import scalafx.collections.ObservableBuffer

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Panel that allows user to manage band, mode, operator etc.
 * Changes to controls taake place immediately.
 *
 * @param currentStationProperty what this edits.
 * @param bandFactory        available bands and modes.
 * @param knownOperatorsProperty Operators who have used fdcluster.
 */
class StationPanel @Inject()(currentStationProperty: StationProperty,
                             allContestRules: AllContestRules,
                             knownOperatorsProperty: KnownOperatorsProperty,
                             rigInfo: RigInfo) {
  val rigState = new Label()
 rigState.text <== rigInfo.rigState

  allContestRules.contestRulesProperty.onChange{(_,_,nv) =>
    setup(nv)
  }

  val band: ComboBox[String] = new ComboBox[String]() {
    value <==> currentStationProperty.bandNameProperty
  }
  val mode: ComboBox[String] = new ComboBox[String]() {
    value <==> currentStationProperty.modeNameProperty
  }
  val operator: ComboBox[CallSign] = new ComboBox[CallSign](knownOperatorsProperty.value.callSigns) {
    editable.value = true
  }
  private val currentOperator: String = currentStationProperty.operatorProperty.value
  operator.setValue(currentOperator)

  operator.onAction = (event: ActionEvent) => {
    val currentEditText = operator.editor.value.text.value
    currentStationProperty.operatorProperty.value = currentEditText
    val items: ObservableList[String] = operator.items.value
    if (!items.contains(currentEditText)) {
      items.add(currentEditText)
      knownOperatorsProperty.add(currentEditText)
    }
  }

  val rig: TextField = new  TextField(){
    text <==> currentStationProperty.rigProperty
    tooltip = "Rig currently being used at this node."
  }
    val antenna: TextField = new  TextField(){
    text <==> currentStationProperty.antennaProperty
      tooltip = "Antenna currently being used at this node."
  }


  val goc = new GridOfControls(4-> 5)
  goc.addControl("Rig", rigState)
  goc.addControl("Band", band)
  goc.addControl("Mode", mode)
  goc.addControl("Op", operator)
  goc.addControl("Rig", rig)
  goc.addControl("Antenna", antenna)
  forceCaps(operator.editor.value)
  val pane: GridOfControls = goc


  private def setup(contestRules:ContestRules):Unit= {
    band.items = ObservableBuffer.from(contestRules.bands.bands)
    mode.items = ObservableBuffer.from(contestRules.modes.modes)
  }

  setup(allContestRules.currentRules)

}


