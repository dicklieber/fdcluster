
package org.wa9nnn.fdcluster.store.network

import java.net._
import java.nio.channels.DatagramChannel

import akka.actor.{ActorRef, Props}
import akka.io.Inet.SO.ReuseAddress
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.{Codec, DistributedQsoRecord}
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.JsonContainer
import play.api.libs.json.Json
import org.wa9nnn.fdcluster.model.MessageFormats._

class MulticastListenerActor(inetAddress: InetAddress, val config: Config) extends MulticastActor with LazyLogging {

  import context.system

  private val receiveBindAddress = new InetSocketAddress(multicastGroup, port)
  IO(Udp) ! Udp.Bind(self, receiveBindAddress, List(ReuseAddress(true), Inet4ProtocolFamily(), MulticastGroup()))

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      logger.info("Multicast listener started.")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, _) =>
      val codec = decode(data)
      codec.foreach { c ⇒
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

  final case class MulticastGroup() extends SocketOptionV2 with LazyLogging {
    override def afterBind(s: DatagramSocket): Unit = {
      val networkInterface = NetworkInterface.getByInetAddress(inetAddress)
      s.getChannel.join(multicastGroup, networkInterface)
    }
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
  def props(inetAddress: InetAddress, config: Config): Props =  Props(new MulticastListenerActor(inetAddress, config))
}