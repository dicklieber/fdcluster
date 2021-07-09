package org.wa9nnn.fdcluster.store.network

import akka.actor.{Actor, ActorRef}
import akka.io.Udp.SO
import akka.io.{IO, Udp, UdpConnected}
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.http.{HttpClientActor, HttpClientSender}
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, ClusterSender, StoreMessage}
import org.wa9nnn.fdcluster.store.{JsonContainer, StoreSender}
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.net.InetSocketAddress
import java.time.Duration
import javax.inject.Inject

class BroadcastListener @Inject()(config:Config, cluster: ClusterSender, store:StoreSender) extends Actor with LazyLogging with DefaultInstrumented{
  private val broadcast: SO.Broadcast = akka.io.Udp.SO.Broadcast(true)
  val socketTimeout: Duration = config.getDuration("fdcluster.broadcast.timeout")
  val port: Int = config.getInt("fdcluster.broadcast.port")
  import context.system
  val messagesReceivedMeter: scala.Meter = metrics.meter("messagesReceived")


  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", port), Seq(broadcast))


  def receive: Receive = {
    case Udp.Bound(local) =>
      context.become(ready(sender()))
    case x =>
      println(x)
  }
  def ready(connection: ActorRef): Receive = {
    case Udp.Received(data: ByteString, from) =>
      for {
        jc <- JsonContainer(data)
        rec <- jc.received()
      } {
        messagesReceivedMeter.mark()
        logger.whenTraceEnabled {
          logger.trace(s"Got: $jc from  ${from}")
        }
        rec match {
          case sm: StoreMessage =>
            store ! sm
          case cm: ClusterMessage =>
            cluster ! cm
        }
      }






    case UdpConnected.Disconnect =>
      connection ! UdpConnected.Disconnect
    case UdpConnected.Disconnected =>
      context.stop(self)

    case x =>
      println(x)
  }

}
