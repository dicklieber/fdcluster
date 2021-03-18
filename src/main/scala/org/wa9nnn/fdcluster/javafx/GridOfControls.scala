
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

package org.wa9nnn.fdcluster.javafx

import javafx.scene.control.SingleSelectionModel
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.geometry.Insets
import scalafx.scene.control.{ComboBox, Label, TextArea, TextField}
import scalafx.scene.layout.GridPane
import scalafx.scene.text.Text

import java.text.NumberFormat
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

/**
 * Help to build a GridPane of one column of labeled controls.
 */
class GridOfControls extends GridPane {
  hgap = 10
  vgap = 10
  padding = Insets(20, 100, 10, 10)
  implicit val row = new AtomicInteger()

  private def label(label: String): Int = {
    val r = row.getAndIncrement()
    add(new Label(label + ":"), 0, r)
    r
  }

  def addText(labelText: String, defValue: String): StringProperty = {
    val row = label(labelText)
    val control = new TextField()
    control.text = defValue
    add(control, 1, row)
    control.text
  }

  def addInt(labelText: String, defValue: Int): IntegerProperty = {
    val row = label(labelText)

    val control = new TextField()
    control.text = NumberFormat.getNumberInstance.format(defValue)

    add(control, 1, row)
    val integerProperty = IntegerProperty(defValue)
    control.text.onChange { (_, _, nv) =>
      integerProperty.value = NumberFormat.getNumberInstance.parse(nv)
    }
    integerProperty
  }

  def addDuration(labelText: String, defValue: Duration): ObjectProperty[Duration] = {
    val row = label(labelText)
    val control = new TextField()
    control.text = defValue.toString()
    add(control, 1, row)
    val durProperty = ObjectProperty(defValue)
    control.text.onChange { (_, _, nv) =>
      durProperty.value = Duration.parse(nv)
    }
    durProperty
  }


  def addTextArea(labelText: String, defValue: String, nRows: Int = 3): StringProperty = {
    val row = label(labelText)
    val control = new TextArea(defValue) {
      prefRowCount = nRows
    }
    add(control, 1, row)
    control.text
  }

  def addCombo[T](labelText: String, choices: Seq[T], defValue: Option[T]): SingleSelectionModel[T] = {
    val row = label(labelText)
    val control = new ComboBox[T](choices)
    val value: SingleSelectionModel[T] = control.selectionModel.value
    defValue.foreach { d =>
      value.select(d)
    }
    add(control, 1, row)
    value
  }
}
