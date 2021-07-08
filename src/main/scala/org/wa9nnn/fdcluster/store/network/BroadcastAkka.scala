package org.wa9nnn.fdcluster.store.network

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Udp.SO
import akka.io.{IO, Udp, UdpConnected}
import akka.util.ByteString

import java.net.InetSocketAddress

object BroadcastAkka extends App {
  private val actorSystem: ActorSystem = ActorSystem()
  private val actorRef: ActorRef = actorSystem.actorOf(Props(classOf[BroadcastAkka]), "AkkaBroadcast")
  var sn = 0
  while (true) {
    Thread.sleep(3000)

    actorRef ! s"$sn: Now is the time."
    sn = sn + 1
  }

  def props(): Props = Props(new BroadcastAkka())
}

class BroadcastAkka extends Actor {

  import context.system

  private val broadcastSocketAddress = new InetSocketAddress("255.255.255.255", 1502)
  private val broadcast: SO.Broadcast = akka.io.Udp.SO.Broadcast(true)
  //  IO(Udp) ! Udp.Bind(self,  new InetSocketAddress("localhost", 0), Seq(broadcast))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 1502), Seq(broadcast))
  //  IO(Udp) ! Udp.SimpleSender(  Seq(broadcast)
  //  )

  def receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
    case Udp.Bound(local) =>
      context.become(ready(sender()))

    case x =>
      println(x)
  }

  def ready(connection: ActorRef): Receive = {
    case Udp.Received(data, from) =>
      val sData = new String(data.toArray)
      println(s"from: $from data:$sData")
    // process data, send it on, etc.
    case msg: String =>
      connection ! Udp.Send(ByteString(msg), broadcastSocketAddress)
    case UdpConnected.Disconnect =>
      connection ! UdpConnected.Disconnect
    case UdpConnected.Disconnected =>
      context.stop(self)

    case x =>
      println(x)
  }
}