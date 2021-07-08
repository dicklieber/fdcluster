package org.wa9nnn.fdcluster.store.network

import akka.actor.ActorRef
import com.github.andyglow.config.ConfigOps
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, StoreMessage}
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.DatagramPacket


abstract class JsonContainerSender(
                                    store: ActorRef,
                                    cluster: ActorRef,
                                    config: Config) extends DefaultInstrumented with LazyLogging {
   val port: Int = config.get[Int]("port")
   val bufferLength = 1400

  val messagesReceivedMeter: scala.Meter = metrics.meter("messagesReceived")
  val messagesSentMeter: scala.Meter = metrics.meter("messagesSent")
  val messageSizeStats = new DescriptiveStatistics()
  metrics.gauge("MessageSize:Mean") {
    messageSizeStats.getMean
  }
  metrics.gauge("MessageSize:Max") {
    messageSizeStats.getMax
  }
  metrics.gauge("MessageSize:Min") {
    messageSizeStats.getMin
  }
  //  private implicit val timeout: Timeout = Timeout(5 seconds)

  def send(jsonContainer: JsonContainer): Unit

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


}