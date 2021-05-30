package org.wa9nnn.fdcluster.store.network.multicast


import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.andyglow.config._
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket}
import java.util.{Timer, TimerTask}
import scala.compat.java8.DurationConverters.DurationOps
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, NodeStatus, StoreMessage}
import org.wa9nnn.fdcluster.store.network.JsonContainerSender
import org.wa9nnn.fdcluster.store.{JsonContainer, RequestNodeStatus}

import javax.inject.Inject
import _root_.scala.language.postfixOps

class MulticastThing @Inject()(clusterControl: NetworkControl, config: Config,
                               @Named("store") val store: ActorRef,
                               @Named("cluster") val cluster: ActorRef,
                               nodeAddress: NodeAddress) extends LazyLogging with DefaultInstrumented  with JsonContainerSender{
  logger.info("Using MulticastThing")
  private val multastConfig: Config = config.getConfig("fdcluster.multicast")
  private val port: Int = multastConfig.get[Int]("port")
  private val multicastGroup =  InetAddress.getByName( multastConfig.getString("group"))
//  val multicastAddress = new InetatSocketAddress(multicastGroup, port)

  val timeoutMs: Int = multastConfig.getDuration("timeout").duration.toMillis.toInt
  val heartbeatMs: Long = multastConfig.getDuration("heartbeat").duration.toMillis
  var heartbeatTimer: Option[Timer] = None
  private val messagesMeter: scala.Meter = metrics.meter("messages")
  private implicit val timeout: Timeout = Timeout(5 seconds)


  val multicastSocket = new MulticastSocket(port)
  multicastSocket joinGroup multicastGroup
//  socket.setBroadcast(true)
  multicastSocket.setSoTimeout(timeoutMs)

  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    shutdown()
  }, "Broadcast Shutdown"))

  resetHeartBeatTimer()

  val datagramSocket = new DatagramSocket()

  def send(jsonContainer: JsonContainer): Unit = {
    resetHeartBeatTimer()
    val bytes = jsonContainer.bytes
    val datagramPacket = new DatagramPacket(bytes, bytes.length, multicastGroup, port)
    datagramSocket.send(datagramPacket)
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
        multicastSocket.receive(datagramPacket)
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
    multicastSocket.leaveGroup(multicastGroup)
    multicastSocket.close()
    logger.info("MulticastListener shutdown.")
  }

}
