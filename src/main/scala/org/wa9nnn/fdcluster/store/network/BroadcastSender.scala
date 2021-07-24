package org.wa9nnn.fdcluster.store.network

import akka.actor.{Actor, ActorRef, Cancellable, PoisonPill, Scheduler}
import akka.io.Udp.SO
import akka.io.{IO, Udp, UdpConnected}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.kv
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.{JsonContainer, RequestNodeStatus}

import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class BroadcastSender(config: Config) extends Actor with LazyLogging {
  val heartBeatDuration: Duration = config.getDuration("fdcluster.broadcast.heartbeat")
  val port: Int = config.getInt("fdcluster.broadcast.port")

  private val broadcast: SO.Broadcast = akka.io.Udp.SO.Broadcast(true)
  private val broadcastSocketAddress = new InetSocketAddress("255.255.255.255", port)
  private val scheduler: Scheduler = context.system.scheduler
  private var cancellable: Option[Cancellable] =  Option(scheduler.scheduleOnce(heartBeatDuration, self, SendHeartbeat, global, self))
  private implicit val timeout: Timeout = Timeout(50 seconds)
  val storeActor: ActorRef = context.parent
  import context.system

  IO(Udp) ! Udp.SimpleSender(Seq(broadcast))
  var maybeUdpActor: Option[ActorRef] = None

  def receive: Receive = {
    case Udp.SimpleSenderReady =>
      val ref = sender()
      maybeUdpActor = Option(ref)
      context.become(ready(ref))

    case x =>
      logger.error(s"Unexpected pre-ready message: $x")
  }

  def ready(connection: ActorRef): Receive = {

    case jc: JsonContainer =>
      cancellable.foreach { c =>
        c.cancel()
        cancellable = None
      }

      import scala.concurrent.ExecutionContext.Implicits.global



      connection ! Udp.Send(jc.toByteString, broadcastSocketAddress)
      cancellable = Option(scheduler.scheduleOnce(heartBeatDuration, self, SendHeartbeat, global, self))

    case SendHeartbeat =>
      (storeActor ? RequestNodeStatus).mapTo[NodeStatus].map{ ns =>
        ns.heartBeatMessage
      }.pipeTo(self)

    case UdpConnected.Disconnect =>
      connection ! UdpConnected.Disconnect
    case UdpConnected.Disconnected =>
      context.stop(self)

    case x =>
      logger.error(s"Unexpected ready state message: $x")

  }

  override def postStop(): Unit = {
    maybeUdpActor.foreach(_ ! PoisonPill)
  }

}


case object SendHeartbeat