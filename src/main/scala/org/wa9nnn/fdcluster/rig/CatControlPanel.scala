
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



case class SerialPortSettings(rigSettings: RigSettings)

object SerialPortSettings {
  val baudRates = Seq("115200", "57600", "38400", "19200", "9600", "4800", "1200")
  val defautBaudRate: String = baudRates(3)

}