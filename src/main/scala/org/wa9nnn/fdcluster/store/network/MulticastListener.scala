package org.wa9nnn.fdcluster.store.network

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.typesafe.config.Config
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, StoreMessage}
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.{DatagramPacket, MulticastSocket}
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
                                   val config: Config) extends MulticastActor with Runnable with DefaultInstrumented {
  private val messagesMeter: scala.Meter = metrics.meter("messages")
  private val descriptiveStatistics = new SynchronizedDescriptiveStatistics()

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

    val recv: DatagramPacket = new DatagramPacket(buf, buf.length)
    do {
      try {
        multicastSocket.receive(recv)
        val data = recv.getData
        for {
          jc <- JsonContainer(data)
          rec <- jc.received()
        } {
          descriptiveStatistics.addValue(1.0)
          messagesMeter.mark()
          whenTraceEnabled { () =>
            s"Got: $jc from  ${recv.getAddress}"
          }
          rec match {
            case sm: StoreMessage =>
              store ! sm
            case cm: ClusterMessage =>
              cluster ! cm
          }
        }
      }
      catch {
        case e: Exception =>
          logger.error("MulticastListener", e)
      }
    }
    while (true)
  }

  def shutdown(): Unit = {
    multicastSocket.leaveGroup(multicastGroup)
    multicastSocket.close()
    logger.info("MulticastListener shutdown.")
  }
}
