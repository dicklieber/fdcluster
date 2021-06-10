
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

import java.nio.file.{Path, Paths}

object Serial extends App {
  lazy val ports: Seq[SerialPort] = {
    if (System.getProperty("os.name").toLowerCase.contains("win"))
      Seq.tabulate(15)(SerialPort(_))
    else
      Paths.get("/dev")
        .toFile
        .list
        .filter(_.startsWith("tty."))
        .filterNot(_.contains("Bluetooth-Incoming-Port"))
        .map(f => SerialPort(Paths.get("/dev").resolve(f)))
  }
}

case class SerialPort private(port: String, display: String)

object SerialPort {
  def apply(path: Path): SerialPort = {
    SerialPort(path.toString, path.getFileName.toString.stripPrefix("tty."))
  }

  def apply(comPortNumber: Int): SerialPort = {
    val c: String = s"COM$comPortNumber"
    SerialPort(c, c)
  }
}

