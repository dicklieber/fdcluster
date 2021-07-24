package org.wa9nnn.util

import scala.util.matching.Regex

case class HostPort(host: String, port: Int) {
  override def toString: String = {
    s"$host:$port"
  }
}

object HostPort {
  val parse: Regex = """([\w\.]+):?(\d+)?""".r

  def apply(hostPort: String = "", defaultPort: Int): HostPort = {
    val parse(sHost, sPort) = hostPort
    if (sPort == null)
      new HostPort(sHost, defaultPort)
    else
      new HostPort(sHost, sPort.toInt)
  }
}
