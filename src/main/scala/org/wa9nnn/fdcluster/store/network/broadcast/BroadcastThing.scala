package org.wa9nnn.fdcluster.store.network.broadcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.andyglow.config._
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, NodeStatus, StoreMessage}
import org.wa9nnn.fdcluster.store.{JsonContainer, RequestNodeStatus}

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress}
import java.util.{Timer, TimerTask}
import javax.inject.{Inject, Singleton}
import scala.compat.java8.DurationConverters.DurationOps
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented

import _root_.scala.language.postfixOps

class BroadcastThing (clusterControl: NetworkControl, config: Config,
                               @Named("store") val store: ActorRef,
                               @Named("cluster") val cluster: ActorRef,
                               nodeAddress: NodeAddress) extends LazyLogging with DefaultInstrumented {
  private val broadcastConfig: Config = config.getConfig("fdcluster.broadcast")
  private val port: Int = broadcastConfig.get[Int]("port")
  val broadcastAddress = new InetSocketAddress("255.255.255.255", port)
  val timeoutMs: Int = broadcastConfig.getDuration("timeout").duration.toMillis.toInt
  val heartbeatMs: Long = broadcastConfig.getDuration("heartbeat").duration.toMillis
  var heartbeatTimer: Option[Timer] = None
  private val messagesMeter: scala.Meter = metrics.meter("messages")
  private implicit val timeout: Timeout = Timeout(5 seconds)


  val socket = new DatagramSocket(port)
  socket.setBroadcast(true)
  socket.setSoTimeout(timeoutMs)

  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    shutdown()
  }, "Broadcast Shutdown"))

  resetHeartBeatTimer()

  def send(jsonContainer: JsonContainer): Unit = {
    resetHeartBeatTimer()
    val bytes = jsonContainer.bytes
    val datagramPacket = new DatagramPacket(bytes, bytes.length, broadcastAddress)
    socket.send(datagramPacket)
  }

  def sendHeartBeat(): Unit = {
    try {
      logger.trace("Heartbeat")
      val eventualNodeStatus: Future[NodeStatus] = (store ? RequestNodeStatus).mapTo[NodeStatus]
      val nodeStatus = Await.result[NodeStatus](eventualNodeStatus, 50.seconds)
      send(JsonContainer(nodeStatus))
    } catch {
      case e: Exception =>
        logger.error(s"Waiting for nodeStatus", e)
    } finally {
      resetHeartBeatTimer()
    }
  }

  var buf = new Array[Byte](1000)

  new Thread(() => {

    logger.info(s"Listening for broadcast on port:$port")
    do {
      try {
        val datagramPacket: DatagramPacket = new DatagramPacket(buf, buf.length)
        socket.receive(datagramPacket)
        processMessage(datagramPacket)
      } catch {
        case e: Exception =>
          logger.error(s"Waiting for broadcast message", e)
      }
    } while (true)

  }).start()

  def processMessage(datagramPacket: DatagramPacket): Unit = {
    val data: Array[Byte] = datagramPacket.getData

    for {
      jc <- JsonContainer(data)
      rec <- jc.received()
    } {
      messagesMeter.mark()
      logger.whenTraceEnabled {
        logger.trace(s"Got: $jc from  ${datagramPacket.getAddress}")
      }
      rec match {
        case sm: StoreMessage =>
          store ! sm
        case cm: ClusterMessage =>
          cluster ! cm
      }
    }

  }

  def resetHeartBeatTimer(): Unit = {
    heartbeatTimer.foreach(_.cancel())
    heartbeatTimer = Option {
      val timer = new Timer("Heartbeat", true)
      timer schedule(new TimerTask {
        override def run(): Unit = sendHeartBeat()
      }, heartbeatMs)
      timer
    }
  }

  def shutdown(): Unit = {
    socket.close()
    logger.info("MulticastListener shutdown.")
  }

}
