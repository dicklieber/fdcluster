
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

class RigIo(socketAdapter: SocketAdapter) extends Rig {

  def get(command: String): Seq[String] = {
    socketAdapter.doOp(command)
  }

  def getInt(command: String): Int = {
    val value = get(command)
    value.map { s ⇒ s.toInt }.head
  }

  override def frequency: Int = {
    getInt("f")
  }

  override def modeAndBandWidth: (String, Int) = {
    val lines = get("m")
    (lines.head, lines(1).toInt)
  }

  override def radio: String = ""

  override def caps: Map[String, String] = {
    (for {
      cap <- get("1")
    } yield {
      val strings: Array[String] = cap.split(":").map(_.trim)
      val length = strings.length
      val rValue = if(length > 1) strings(1) else ""
      (strings.head, rValue)
    }).toMap
  }
}

object RigIo {
  val defaultPort = 4532

  def apply(hostAndPort: String): RigIo = {
    //todo hande port
    new RigIo(SocketAdapter(hostAndPort, defaultPort))
  }

  def main(args: Array[String]): Unit = {
    val rigIo = RigIo("127.0.0.1")
//    val rigIo = RigIo("192.168.0.177")
    println(rigIo.modeAndBandWidth)
    println(rigIo.frequency)

  }
}