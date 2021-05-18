package org.wa9nnn.fdcluster.store.network

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.ClusterControl
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, StoreMessage}
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.{DatagramPacket, MulticastSocket, SocketTimeoutException}
import javax.inject.{Inject, Singleton}

/**
 * Listens for multicast messages, decodes and send to the Store Actor.
 *
 * @param cluster Actor.
 * @param store   Actor.
 * @param config
 */
@Singleton
class MulticastListener @Inject()(
                                   @Named("cluster") val cluster: ActorRef,
                                   @Named("store") val store: ActorRef,
                                   val config: Config,
                                   clusterControl: ClusterControl)
  extends MulticastActor with Runnable with DefaultInstrumented with LazyLogging {
  private val messagesMeter: scala.Meter = metrics.meter("messages")

  private var multicastSocket = new MulticastSocket(port)

  private val multicastListenerThread = new Thread(this, "MulticastListener")
  multicastListenerThread.setDaemon(true)
  multicastListenerThread.start()

  override def run(): Unit = {
    logger.debug("Starting MulticastListener thread.")
    multicastSocket.setReuseAddress(true)
    multicastSocket.joinGroup(multicastGroup)

    val thread = new Thread(() => {
      logger.info("Running MulticastListener shutdown hook.")
      shutdown()
    }, "MulticastListener Shutdown")
    Runtime.getRuntime.addShutdownHook(thread)


    var buf = new Array[Byte](8000)

    val datagramPacket: DatagramPacket = new DatagramPacket(buf, buf.length)
    do {
      try {
        multicastSocket.setSoTimeout(timeoutMs)
        multicastSocket.receive(datagramPacket)
        if (clusterControl.isUp)
          processMessage(datagramPacket)
      }
      catch {
        case _: SocketTimeoutException =>
          logger.error(s"Timeout waiting for multicast message! $duration")
        case e: Exception =>
          logger.error("MulticastListener", e)
      }
    }
    while (true)
  }

  def processMessage(datagramPacket: DatagramPacket): Unit = {
    val data: Array[Byte] = datagramPacket.getData

    for {
      jc <- JsonContainer(data)
      rec <- jc.received()
    } {
      messagesMeter.mark()
      logger.whenTraceEnabled {
         logger.trace( s"Got: $jc from  ${datagramPacket.getAddress}")
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
    multicastSocket.leaveGroup(multicastGroup)
    multicastSocket.close()
    logger.info("MulticastListener shutdown.")
  }
}
