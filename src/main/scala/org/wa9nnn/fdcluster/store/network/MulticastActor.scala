
package org.wa9nnn.fdcluster.store.network

import java.net.InetAddress

import akka.actor.Actor
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

abstract class MulticastActor extends Actor with LazyLogging {
   val config: Config //= context.system.settings.config
  private val multicastConfig = config.getConfig("fdcluster.multicast")
  val port: Int = multicastConfig.getInt("port")
  val multicastGroup: InetAddress = InetAddress.getByName(multicastConfig.getString("group"))

}
