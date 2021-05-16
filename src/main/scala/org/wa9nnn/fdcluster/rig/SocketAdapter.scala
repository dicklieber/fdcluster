
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

import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket

/**
 * Everything we need for hamlib from a socket
 * Allows easy mocking. Mocking [[java.net.Socket]] is hard.
 */
trait SocketAdapter {
  def doOp(command: String):Seq[String]
}

class SocketAdapterSocket(socket: Socket) extends SocketAdapter {

  private val out = new PrintWriter(socket.getOutputStream, true)
  private val streamReader = new InputStreamReader(socket.getInputStream)
  private val in = new BufferedReader(streamReader)

  override def doOp(command: String): Seq[String] = {
    out.println(command)
    val lines = List.newBuilder[String]
    while (!in.ready()) {
      Thread.sleep(10)
    }
    while (in.ready()) {
      lines += in.readLine()
    }
    val rr = lines.result()
//rr.foreach(l => println(s""""$l","""))
    rr
  }

  override def toString: String = {
    socket.toString
  }
}

object SocketAdapter extends LazyLogging{
  def apply(hostAndPort: String, defaultPort: Int): SocketAdapter = {
    new SocketAdapterSocket(socketFromString(hostAndPort, defaultPort))
  }

  def hostPortParse(hostAndPort: String, defaultPort: Int): (String, Int) = {
    val strings = hostAndPort.split(":").map(_.trim)
    (strings.head,
      if (strings.length > 1)
        strings(1).toInt
      else
        defaultPort
    )
  }

  def socketFromString(hostAndPort: String, defaultPort: Int): Socket = {
    val (host, port) = hostPortParse(hostAndPort, defaultPort)
    try {
      val s = new Socket(host, port)
      s
    } catch {
      case e: Throwable =>
        logger.error(s"rigctld $host:$port failed: ${e.getMessage}!")
        throw e
    }
  }

}
