
package org.wa9nnn.fdlog.store.network

import java.net._
import java.net.MulticastSocket
import java.net.InetAddress


class multicaster {
  val port = 5000
  val group = "225.4.5.6"



  val multicastSocket = new MulticastSocket(port)
  multicastSocket.joinGroup(InetAddress.getByName(group))
}
