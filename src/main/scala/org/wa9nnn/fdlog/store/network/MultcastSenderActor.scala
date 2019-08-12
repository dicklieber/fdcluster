
package org.wa9nnn.fdlog.store.network

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.io.{IO, Udp}
import org.wa9nnn.fdlog.model.DistributedQsoRecord

class MultcastSenderActor() extends MulticastActor {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case qso: DistributedQsoRecord =>
      val byteString = qso.toByteString
      send ! Udp.Send(byteString, new InetSocketAddress(multicastGroup, port))
    case x ⇒
      println(s"Unexpected: $x")
  }
}

object MultcastSenderActor {
  def props(): Props = {
    Props(new MultcastSenderActor())
  }
}
