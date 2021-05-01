
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

import javafx.collections.ObservableList
import org.wa9nnn.fdcluster.model.CallSign._
import org.wa9nnn.fdcluster.model.CallSign
import org.wa9nnn.fdcluster.model.{BandModeFactory, CurrentStationProperty, KnownOperatorsProperty}
import org.wa9nnn.util.InputHelper.forceCaps
import _root_.scalafx.Includes._
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.scene.control.{ComboBox, Control, Label, TextField}
import _root_.scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Panel that allows user to manage band, mode, operator etc.
 * Changes to controls taake place immediately.
 *
 * @param currentStationProperty what this edits.
 * @param bandModeFactory        available bands and modes.
 * @param knownOperatorsProperty Operators who have used fdcluster.
 */
class CurrentStationPanel @Inject()(currentStationProperty: CurrentStationProperty, bandModeFactory: BandModeFactory, knownOperatorsProperty: KnownOperatorsProperty) extends GridPane {
  val rigFreq = new Label()
  val band: ComboBox[String] = new ComboBox[String](bandModeFactory.availableBands.sorted.map(_.band)) {
    value <==> currentStationProperty.bandNameProperty
  }
  val mode: ComboBox[String] = new ComboBox[String](bandModeFactory.modes.map(_.mode)) {
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
    val items: ObservableList[CallSign] = operator.items.value
    if (!items.contains(currentEditText)) {
      items.add(currentEditText)
      knownOperatorsProperty.add(currentEditText)
    }
  }

  val rig: TextField = new  TextField(){
    text <==> currentStationProperty.rigProperty
  }
    val antenna: TextField = new  TextField(){
    text <==> currentStationProperty.antennaProperty
  }

  val row = new AtomicInteger()

  def add(label: String, control: Control, maybeTooltip:Option[String] = None): Unit = {
    val nrow = row.getAndIncrement()
    add(new Label(label + ":"), 0, nrow)
    add(control, 1, nrow)
    maybeTooltip.foreach{control.tooltip = _}
  }

  add("Rig", rigFreq)
  add("Band", band)
  add("Mode", mode)
  add("Op", operator)
  add("Rig", rig, Some("Rig currently being used at this node."))
  add("Antenna", antenna, Some("Antenna currently being used at this node."))
  forceCaps(operator.editor.value)
}


