
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
import org.wa9nnn.fdcluster.model.{BandModeFactory, BandModeOperatorStore}
import org.wa9nnn.util.InputHelper.forceCaps
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{ComboBox, Control, Label}
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class BandModeOpPanel @Inject()(bandModeFactory: BandModeFactory,
                                bandModeOperatorStore: BandModeOperatorStore) extends GridPane {
  val rigFreq = new Label()
  val band: ComboBox[String] = new ComboBox[String](bandModeFactory.avalableBands.sorted.map(_.band)) {
    value <==> bandModeOperatorStore.band
  }
  val mode: ComboBox[String] = new ComboBox[String](bandModeFactory.modes.map(_.mode)) {
    value <==> bandModeOperatorStore.mode
  }
  val operator: ComboBox[String] = new ComboBox[String](bandModeOperatorStore.knownOperators) {
    value <==> bandModeOperatorStore.operator
    editable.value = true
  }
  operator.onAction = (event: ActionEvent) => {
    val currentEditText = operator.editor.value.text.value

    println(s"currentEditText: $currentEditText")
    val items: ObservableList[String] = operator.items.value
    if (!items.contains(currentEditText)) {
      items.add(currentEditText)
    }
  }

  val row = new AtomicInteger()

  def add(label: String, control: Control): Unit = {
    val nrow = row.getAndIncrement()
    add(new Label(label + ":"), 0, nrow)
    add(control, 1, nrow)
  }

  add("Rig", rigFreq)
  add("Band", band)
  add("Mode", mode)
  add("Op", operator)
  forceCaps(operator.editor.value)
}


