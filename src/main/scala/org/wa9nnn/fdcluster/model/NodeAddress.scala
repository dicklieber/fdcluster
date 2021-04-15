
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

package org.wa9nnn.fdcluster.model

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import javax.inject.Inject
import scala.jdk.CollectionConverters._

/**
 * Identifies one node in the cluster.
 * For testing there can be more than one node at an IP address. So the instance is used to qualify them.
 * This is not suitable to send messages to a node. That's in org.wa9nnn.fdcluster.model.sync.NodeStatus#apiUrl()
 *
 * @param host     ip address of this node.
 * @param instance from application.conf or command line e.g -Dinstance=2
 * @param httpPort as opposed to the multicast port.
 */
case class NodeAddress @Inject()(host: String = "localhost", instance: Int = 0, httpPort: Int = 8000) extends Ordered[NodeAddress] {
  val display: String = {
    s"$host:$instance"
  }
  val graphiteName: String = {
    s"${host.replace('.', '_')}:$instance"
  }

  val url: URL = {
    new URL("http", host, httpPort, "")
  }

  def uri: Uri = {
    Uri()
      .withHost(host)
      .withPort(httpPort)
  }

  val inetAddress: InetAddress = InetAddress.getByName(host)

  override def compare(that: NodeAddress): Int = {
    that match {
      case address: NodeAddress =>
        var ret = address.host compareTo that.host
        if (ret == 0) {
          ret = this.instance compareTo that.instance
        }
        ret
      case _ => -1
    }
  }
}

object NodeAddress {
  def apply(config: Config): NodeAddress = {
    val instance = config.getInt("instance")
    val httpPort = config.getInt("fdcluster.http.port")
    NodeAddress(determineIp().getHostAddress, instance, httpPort)
  }

  /**
   *
   * @return this 1st  V4 address that is not the loopback address.
   */
  def determineIp(): InetAddress = {
    (for {
      networkInterface <- NetworkInterface.getNetworkInterfaces.asScala.toList
      inetAddresses <- networkInterface.getInetAddresses.asScala
      if !inetAddresses.isLoopbackAddress && inetAddresses.isInstanceOf[Inet4Address]
    } yield {
      inetAddresses
    })
      .headOption.getOrElse(throw new IllegalStateException("No IP address!"))

  }

}
