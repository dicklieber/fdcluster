
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

import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.geometry.Insets
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.GridPane
import org.wa9nnn.fdcluster.rig.SerialPortSettings.baudRates
import scalafx.util.StringConverter

class CatControlPanel(serialPortSettings: SerialPortSettings) extends GridPane {

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
    serialPortSettings.port.foreach { sp =>
      value = sp
    }
  }
  val baudRateComboBox = new ComboBox[String](ObservableBuffer.from(baudRates))
  baudRateComboBox.setValue(serialPortSettings.baudRate)

  //  val gridPane: GridPane = new GridPane() {
  hgap = 10
  vgap = 10
  padding = Insets(20, 100, 10, 10)

  add(new Label("Serial Port:"), 0, 0)
  add(portComboBox, 1, 0)

  add(new Label("Baud Rate:"), 0, 1)
  add(baudRateComboBox, 1, 1)


  def result: SerialPortSettings = {
    SerialPortSettings(Option(portComboBox.value.value), baudRateComboBox.value.value)
  }
}

case class SerialPortSettings(port: Option[SerialPort] = None, baudRate: String = "9600")

object SerialPortSettings {
  val baudRates = Seq("115200", "57600", "38400", "19200", "9600", "4800", "1200")
  val defautBaudRate: String = baudRates(3)

}