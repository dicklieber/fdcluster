
package org.wa9nnn.fdlog.store.network

import java.net._
import java.nio.channels.DatagramChannel

import akka.actor.{ActorRef, Props}
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import org.wa9nnn.fdlog.model.DistributedQsoRecord


class MulticastListenerActor(nextActor: ActorRef) extends MulticastActor {

  import context.system

  private val receiveBindAddress = new InetSocketAddress(port)
  IO(Udp) ! Udp.Bind(self, receiveBindAddress, List(Inet4ProtocolFamily(), MulticastGroup(multicastGroup)))

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      logger.info("Multicast listener started.")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, _) =>
      val dQso = DistributedQsoRecord(data)
      nextActor ! dQso
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

}

final case class MulticastGroup(address: String) extends SocketOptionV2 {
  override def afterBind(s: DatagramSocket): Unit = {
    val group = InetAddress.getByName(address)
    val host = InetAddress.getLocalHost
    val networkInterface = NetworkInterface.getByInetAddress(host)
    s.getChannel.join(group, networkInterface)
  }
}

final case class Inet4ProtocolFamily() extends DatagramChannelCreator {
  override def create(): DatagramChannel =
    DatagramChannel.open(StandardProtocolFamily.INET)
}

object MulticastListenerActorXX {
  def props(parent: ActorRef): Props = Props(new MulticastListenerActor(parent))
}