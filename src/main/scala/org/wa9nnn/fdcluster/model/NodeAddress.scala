
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
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.javafx.cluster._
import play.api.libs.json._

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

/**
 * Identifies one node in the cluster.
 * For testing there can be more than one node at an IP address. So the instance is used to qualify them.
 * This is not suitable to send messages to a node. That's in org.wa9nnn.fdcluster.model.sync.NodeStatus#apiUrl()
 *
 * @param url       this node.
 * @param instance  from application.conf or command line e.g -Dinstance=2
 */
//case class NodeAddress(ipAddress: String = "", hostName: String = "localhost", instance: Option[Int] = None, port: Int = 8080)
case class NodeAddress(url: URL = new URL("http:///"), instance: Option[Int] = None)
  extends Ordered[NodeAddress]
    with NodeValueProvider
    with PropertyCellName {

  val inetAddress: InetAddress = InetAddress.getByName(url.getHost)
  lazy val instancePart: String = instance.map(i => s";$i").getOrElse("")

  lazy val hostPort:String = {
    val host = url.getHost
    val port = url.getPort
    s"$host:$port"
  }


  override def collectNamedValues(namedValueCollector: NamedValueCollector): Unit = {
    import org.wa9nnn.fdcluster.javafx.cluster.ValueName._

    namedValueCollector(Node, url.getHost)
    namedValueCollector(HTTP, Cell(url.toExternalForm).withUrl(url))
  }

  lazy val displayWithIp: String = {
    val address = InetAddress.getByName(url.getHost).getHostAddress
    s"${url.getHost} $instancePart ($address)"
  }
  val display: String = {
    s"${url.getHost}$instancePart"
  }

  def fileUrlSafe: String = {
    s"${url.getHost}${instancePart}"
  }

  val qsoNode: String = {
    s"$url.getHost:$instance"
  }

  lazy val graphiteName: String = {
    val graphiteSafeUrl = url.getHost.replace('.', '_')
    s"$graphiteSafeUrl${instance.map(instance => s"_$instance").getOrElse("")}"
  }


  lazy val propertyCell: PropertyCell = {
    val cell = Cell(display)
      .withToolTip(toolTip)
      .withCssClass("clusterRowHeader")

    PropertyCellFactory(NamedValue(this, cell))
  }

  def uri: Uri = {
    Uri()
      .withHost(url.getHost)
      .withPort(url.getPort)
  }


  override def compare(that: NodeAddress): Int = {
    val ret = url.toExternalForm compareTo that.url.toExternalForm
    if (ret == 0) {
      val thisI = instance.getOrElse(-1)
      val thatI = that.instance.getOrElse(-1)
      thisI.compareTo(thatI)
    } else
      ret
  }

  override def toolTip: String = "Where this came from."

  override def name: String = display
}


object NodeAddress {
  def apply(instance: Option[Int], config: Config): NodeAddress = {
    val httpPort = config.getInt("fdcluster.httpServer.port")
    val inetAddress = determineIp()
    val address: String = inetAddress.getHostAddress
    val port = instance.map(_ + httpPort).getOrElse(httpPort)
    val url = new URL("http", address, port, "")
    new NodeAddress(url, instance)
  }

  /**
   *
   * @return this 1st  V4 address that is not the loopback address.
   */
  def determineIp(): InetAddress = {
    (for {
      networkInterface <- NetworkInterface.getNetworkInterfaces.asScala.toList
      inetAddresses <- networkInterface.getInetAddresses.asScala
      if !inetAddresses.isLoopbackAddress && inetAddresses.isInstanceOf[Inet4Address] && !inetAddresses.isLinkLocalAddress
    } yield {
      inetAddresses
    })
      .sortBy(_.toString)
      .reverse
      .headOption.getOrElse(throw new IllegalStateException("No IP address!"))

  }

  //  private val r: Regex = """(.*)(?:\|(\d+))?""".r
  /**
   * to make JSON a bit more compact
   */
  implicit val nodeAddressformat: Format[NodeAddress] = new Format[NodeAddress] {
    override def reads(json: JsValue): JsResult[NodeAddress] = {
      val ss = json.as[String]
      try {
        val strings = ss.split("""\|""")
        //        val r(sUrl, i) = ss

        val url = new URL(strings.head)
        val instance = if (strings.length > 1)
          Option(strings(1)).map(_.toInt)
        else
          None
        JsSuccess(new NodeAddress(url, instance))
      } catch {
        case e: Exception =>
          JsError(s"NodeAddress: $ss note parsable!")
      }
    }

    override def writes(na: NodeAddress): JsValue = {
      val instancePart = na.instance.map(instance => s"|$instance").getOrElse("")
      JsString(s"${na.url.toExternalForm}$instancePart")
    }
  }

}
