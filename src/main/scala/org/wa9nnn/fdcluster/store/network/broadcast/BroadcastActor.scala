package org.wa9nnn.fdcluster.store.network.broadcast

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.github.andyglow.config._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.InetSocketAddress

class BroadcastActor(clusterControl: NetworkControl, config: Config) extends Actor with LazyLogging {

  import context.system

  private val port: Int = config.get[Int]("fdcluster.broadcast.port")
  val broadcastAddress = new InetSocketAddress("255.255.255.255", port)

//  IO(Udp) ! Udp.SimpleSender
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", port))

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
    case Udp.Bound(local) =>
      context.become(ready(sender()))

    case f:Udp.CommandFailed =>
      val cmd = f.cmd
      logger.error(s"$cmd")

    case x ⇒
      logger.error(s"BroadcastSendActor: Unexpected: $x")

  }

  def ready(socket: ActorRef): Receive = {

//    case x =>
//      logger.info(s"x: $x")


    case jc: JsonContainer =>
      logger.info(s"jc: $jc")
      socket ! Udp.Send(jc.toByteString, broadcastAddress)
      logger.info(s"jc: after")

    case Udp.Received(data: ByteString, remote: InetSocketAddress) =>
      logger.info(s"Got $data from $remote")
//      val processed =  // parse data etc., e.g. using PipelineStage
//        socket ! Udp.Send(data, remote) // example server echoes back
//      nextActor ! processed

    case Udp.Unbind  =>
      socket ! Udp.Unbind
    case Udp.Unbound =>
      context.stop(self)

    case x ⇒
      logger.error(s"BroadcastSendActor: Unexpected: $x")
  }

}
