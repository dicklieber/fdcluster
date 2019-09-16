
package org.wa9nnn.fdlog.store.network

import java.net.{Inet4Address, InetAddress, NetworkInterface}

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

/**
 *
 */
object IpFinder extends LazyLogging {
  lazy val ourInetAddress: InetAddress = {

    for {
      networkInterface <- NetworkInterface.getNetworkInterfaces.asScala.toList
      inetAddresses <- networkInterface.getInetAddresses.asScala
      if !inetAddresses.isLinkLocalAddress && !inetAddresses.isLoopbackAddress && inetAddresses.isInstanceOf[Inet4Address]
    } yield {
      inetAddresses
    }
    }
    .headOption.getOrElse(throw new IllegalStateException("No IP address!"))
}