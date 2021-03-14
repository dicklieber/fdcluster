
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

import com.fazecast.jSerialComm.SerialPort
import org.wa9nnn.util.StructuredLogging

object Serial extends App {

  def ports: Seq[SerialPortWrapper] = {
    SerialPort.getCommPorts
      .map(SerialPortWrapper)
      .filter(_.useful)
      .sorted
  }
}

case class SerialPortWrapper(serialPort: SerialPort) extends NamedSerialPort[SerialPortWrapper] {
  override val name: String = serialPort.getSystemPortName

  override def description: String = serialPort.getPortDescription
}

trait NamedSerialPort[T <: NamedSerialPort[T]] extends Ordered[T] with StructuredLogging {
  implicit def name: String

  def description: String

  override def toString: String = s"$name ($description)"

  def compare(that: T): Int = {
    name.compareToIgnoreCase(that.name)
  }

  def dd(cmd: String, ret: Boolean)(implicit name: String): Boolean = {
//    println(s"$name: $cmd => $ret")
    ret
  }

  lazy val useful: Boolean = {
    val linuxBad = """ttyp.+""".r
    val microsoftWidows = """COM\d+""".r
    val macOS = """tty\.usb.*""".r
//    val macOSBT = """.*Bluetooth.*""".r
    val raspberryPi = """ttyUSB.*""".r

    name match {
      case linuxBad(_) => dd("linuxBad", ret = false)
      case microsoftWidows(_) => dd("microsoftWidows", ret = true)
      case "tty.Bluetooth-Incoming-Port" => dd("linuxBad", ret = false)
//      case macOSBT(_) => dd("bluetooth", ret = false)
      case raspberryPi(_) => dd("raspberryPi", ret = true) // todo perhaps any Linux
      case macOS(_) => dd("macOS", true) // MacOS
      case d => dd("default", !(d.startsWith("ttyp") || d.contains("Bluetooth")))
    }
  }

}