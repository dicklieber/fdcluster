
package org.wa9nnn.fdlog.store.network

import java.net.InetSocketAddress

import akka.actor.Actor
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

trait MulticastActor extends Actor with LazyLogging {
  val config: Config = context.system.settings.config
  private val multicastConfig = config.getConfig("fdlog.multicast")
  val port: Int = multicastConfig.getInt("port")
  val multicastGroup: String = multicastConfig.getString("group")
}
