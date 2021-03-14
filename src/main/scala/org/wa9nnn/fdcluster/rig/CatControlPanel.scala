
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

import org.wa9nnn.fdcluster.rig.SerialPortSettings.{baudRates, defautBaudRate}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane

class CatControlPanel() extends GridPane {
  def setValue(sps: SerialPortSettings): Unit = {
    portComboBox.setValue(sps.port)
    baudRateComboBox.setValue(sps.baudrate)
  }

  private val serialPortNames: Seq[String] = Serial.ports.map(_.name)
  val portComboBox = new ComboBox[String](ObservableBuffer[String](serialPortNames))
  val baudRateComboBox = new ComboBox[String](ObservableBuffer[String](baudRates))
  baudRateComboBox.setValue(defautBaudRate)

  //  val gridPane: GridPane = new GridPane() {
  hgap = 10
  vgap = 10
  padding = Insets(20, 100, 10, 10)

  add(new Label("Serial Port:"), 0, 0)
  add(portComboBox, 1, 0)

  add(new Label("Baud Rate:"), 0, 1)
  add(baudRateComboBox, 1, 1)


  def result: SerialPortSettings = {
    SerialPortSettings(portComboBox.value.value, baudRateComboBox.value.value)
  }
}

case class SerialPortSettings(port: String, baudrate: String)

object SerialPortSettings {
  val baudRates = Seq("115200", "57600", "38400", "19200", "9600", "4800", "1200")
  val defautBaudRate: String = baudRates(3)

  def apply():SerialPortSettings = SerialPortSettings("-", defautBaudRate)
}