
package org.wa9nnn.fdlog.store.network

import java.net._
import java.nio.channels.DatagramChannel

import akka.actor.{ActorRef, Props}
import akka.io.Inet.SO.ReuseAddress
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import akka.util.ByteString
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.sync.NodeStatus
import org.wa9nnn.fdlog.model.{Codec, DistributedQsoRecord}
import org.wa9nnn.fdlog.store.JsonContainer
import play.api.libs.json.Json


class MulticastListenerActor extends MulticastActor {

  import context.system

  private val group = InetAddress.getByName(multicastGroup)
  private val receiveBindAddress = new InetSocketAddress(group, port)
  IO(Udp) ! Udp.Bind(self, receiveBindAddress, List(ReuseAddress(true), Inet4ProtocolFamily(), MulticastGroup(multicastGroup)))

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      logger.info("Multicast listener started.")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, _) =>
      val codec = decode(data)
      codec.foreach {c ⇒
        context.parent ! c
      }

//    case qso: DistributedQsoRecord =>
//      val byteString = qso.toByteString
//      IO(Udp) ! Udp.Send(byteString, new InetSocketAddress(multicastGroup, port))

    case Udp.Unbind =>
      logger.error("Udp.Unbind")
      socket ! Udp.Unbind
    case Udp.Unbound =>
      logger.error("Udp.Unbound")
      context.stop(self)
    case x ⇒
      logger.error(s"MulticastListenerActor unxpected message: $x")
  }

  /**
   * //todo must be a better way.
   */
  def decode(bs: ByteString): Option[Codec] = {

    val array = bs.toArray
    try {
      val jsonContainer = Json.parse(array).as[JsonContainer]

      val jsObject = Json.parse(jsonContainer.json)
      jsonContainer.className match {
        case "DistributedQsoRecord" ⇒
          Some(jsObject.as[DistributedQsoRecord])
        case "NodeStatus" ⇒
          Some(jsObject.as[NodeStatus])
        case x ⇒
          logger.error(s"Unexpected JSON:${new String(array)}")
          None
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"parsing received message:${new String(array)}")
        None
    }
  }

  override def postStop(): Unit = {
    logger.error(s"postStop: MulticastListenerActor")
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(s"preRestart: reason: ${reason.getMessage}", reason)
    super.preRestart(reason, message)
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
  override def create(): DatagramChannel = {
    val dc = DatagramChannel.open(StandardProtocolFamily.INET)
//    dc.setOption(StandardSocketOptions.SO_REUSEADDR, java.lang.Boolean.TRUE)
    dc
  }
}

object MulticastListenerActor {
  def props: Props = Props(new MulticastListenerActor)
}