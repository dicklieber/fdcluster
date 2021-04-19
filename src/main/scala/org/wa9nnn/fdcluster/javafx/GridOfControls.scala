
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

import org.wa9nnn.util.InputHelper.{forceAllowed, forceCaps => ForceCaps, forceInt => ForceInt}
import scalafx.Includes._
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.util.StringConverter

import java.text.NumberFormat
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import scala.util.matching.Regex

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

   def addText(labelText: String, defValue: String = "",
              forceCaps: Boolean = false,
              regx: Option[Regex] = None,
              tooltip: Option[String] = None): StringProperty = {
    val row = label(labelText)
    val control = new TextField()
    if (forceCaps) {
      ForceCaps(control)
    }
    regx.foreach(regex =>
      forceAllowed(control, regex)
    )
    tooltip.foreach(control.tooltip = _)
    control.text = defValue
    add(control, 1, row)
    control.text
  }

  def addInt(labelText: String, defValue: Int = 0, tooltip: Option[String] = None): IntegerProperty = {
    val row = label(labelText)

    val control = new TextField()
    ForceInt(control)

    tooltip.foreach(control.tooltip = _)
    control.text = NumberFormat.getNumberInstance.format(defValue)

    add(control, 1, row)
    val integerProperty = IntegerProperty(defValue)
    control.text.onChange { (_, _, nv) =>
      integerProperty.value = NumberFormat.getNumberInstance.parse(nv)
    }
    integerProperty
  }

  def addDuration(labelText: String, defValue: Duration, tooltip: Option[String] = None): ObjectProperty[Duration] = {
    val row = label(labelText)
    val control = new TextField()
    tooltip.foreach(control.tooltip = _)
    control.text = defValue.toString
    add(control, 1, row)
    val durProperty = ObjectProperty(defValue)
    control.text.onChange { (_, _, nv) =>
      durProperty.value = Duration.parse(nv)
    }
    durProperty
  }


  def addTextArea(labelText: String, defValue: String = "", nRows: Int = 3, tooltip: Option[String] = None): StringProperty = {
    val row = label(labelText)
    val control = new TextArea(defValue) {
      prefRowCount = nRows
    }
    tooltip.foreach(control.tooltip = _)
    add(control, 1, row)
    control.text
  }

  def addCombo[T](labelText: String,
                  choices: ObservableBuffer[T],
                  defValue: Option[T] = None,
                  tooltip: Option[String] = None,
                  converter: Option[StringConverter[T]] = None): ObjectProperty[T] = {
    val row = label(labelText)
    val control: ComboBox[T] = new ComboBox[T]( ObservableBuffer[T](choices.toSeq))
    converter.foreach(control.converter = _)
    tooltip.foreach(control.tooltip = _)

    val selectionModel: SingleSelectionModel[T] = control.selectionModel.value
    defValue.foreach { d: T =>
      selectionModel.select(d)
    }
    add(control, 1, row)
    control.value
  }

  def addControl(labelText: String, control:Control):Unit = {
    val row = label(labelText)
    add(control, 1, row)
  }
  def add(labelText: String, value:Any):Unit = {
    val row = label(labelText)
    val cell = com.wa9nnn.util.tableui.Cell(value)
    add(Label(cell.value), 1, row)
  }
}
