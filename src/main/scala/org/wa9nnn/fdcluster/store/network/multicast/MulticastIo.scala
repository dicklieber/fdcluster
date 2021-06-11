package org.wa9nnn.fdcluster.store.network.multicast


import akka.actor.ActorRef
import akka.util.Timeout
import com.github.andyglow.config._
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import java.net._
import scala.compat.java8.DurationConverters.DurationOps
import scala.concurrent.duration.DurationInt
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, StoreMessage}
import org.wa9nnn.fdcluster.store.JsonContainer
import org.wa9nnn.fdcluster.store.network.JsonContainerSender

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import _root_.scala.language.postfixOps

@Singleton
class MulticastIo @Inject()(clusterControl: NetworkControl, config: Config,
                            @Named("store") val store: ActorRef,
                            @Named("cluster") val cluster: ActorRef,
                            heartBeat: HeartBeat) extends LazyLogging with DefaultInstrumented with JsonContainerSender {
  logger.info("Using MulticastIo")
  private val bufferLength = 10000

  private val multicastConfig: Config = config.getConfig("fdcluster.multicast")
  private val port: Int = multicastConfig.get[Int]("port")
  private val multicastGroup = InetAddress.getByName(multicastConfig.getString("group"))

  val timeoutMs: Int = multicastConfig.getDuration("timeout").duration.toMillis.toInt
  private val messagesReceivedMeter: scala.Meter = metrics.meter("messagesReceived")
  private val messagesSentMeter: scala.Meter = metrics.meter("messagesSent")
  private val messageSizeStats = new DescriptiveStatistics()
  metrics.gauge("MessageSize:Mean"){
    messageSizeStats.getMean
  }
  metrics.gauge("MessageSize:Max"){
    messageSizeStats.getMax
  }
  metrics.gauge("MessageSize:Min"){
    messageSizeStats.getMin
  }
  private implicit val timeout: Timeout = Timeout(5 seconds)


  val multicastSocket = new MulticastSocket(port)
  multicastSocket joinGroup multicastGroup
  multicastSocket.setSoTimeout(timeoutMs)
  val networkInterface: NetworkInterface = multicastSocket.getNetworkInterface
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    shutdown()
  }, "Broadcast Shutdown"))

  val datagramSocket = new DatagramSocket()

  def send(jsonContainer: JsonContainer): Unit = {
    logger.whenTraceEnabled{
      logger.trace(s"send: $jsonContainer")
    }
    messagesSentMeter.mark()
    heartBeat { () =>
      val bytes = jsonContainer.bytes
      messageSizeStats.addValue(bytes.length)
      if( bytes.length > bufferLength){
        logger.error(s"JsonContainer bytes is larger than receiver buffer! ${bytes.length} > $bufferLength")
      }
      logger.trace(s"Sending ${bytes.length} bytes.")
      val datagramPacket = new DatagramPacket(bytes, bytes.length, multicastGroup, port)
      if (clusterControl.isUp) {
        logger.whenTraceEnabled{
          logger.trace(s"send: Inner")
        }
        datagramSocket.send(datagramPacket)
      }
    }
  }


  var buf = new Array[Byte](bufferLength)

  private var continueListenerThread = true
  private val packetCount = new AtomicInteger()
  new Thread(() => {
    logger.info(s"Listening for multicast on ${multicastGroup.getHostAddress}:$port")
    do {
      try {
        val datagramPacket: DatagramPacket = new DatagramPacket(buf, buf.length)
        logger.trace(s"Waiting for packet received.")
        multicastSocket.receive(datagramPacket)
        logger.trace(s"Multicast packet received. no: ${packetCount.incrementAndGet()}")
        processMessage(datagramPacket)
      } catch {
        case to: SocketTimeoutException =>
          logger.error(s"Waiting for multicast message: ${to.getMessage}")

        case e: Exception =>
          logger.error(s"Waiting for multicast message: ${e.getMessage}")
      }
    } while (continueListenerThread)

  }).start()

  def processMessage(datagramPacket: DatagramPacket): Unit = {
    val data: Array[Byte] = datagramPacket.getData
    val length = datagramPacket.getLength
    val bytes = data.take(length)

    for {
      jc <- JsonContainer(bytes)
      rec <- jc.received()
    } {
      messagesReceivedMeter.mark()
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

  def shutdown(): Unit = {
    continueListenerThread = false
    multicastSocket.leaveGroup(multicastGroup)
    multicastSocket.close()
    logger.info("MulticastListener shutdown.")
  }

}
