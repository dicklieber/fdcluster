
package org.wa9nnn.fdcluster.rig

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket

/**
 * Everything we need for hamlib from a socket
 * Allows easy mocking. Mocking [[Socket]] is hard.
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

object SocketAdapter {
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
        e.printStackTrace()
        throw e
    }
  }

}
