package org.wa9nnn.fdcluster.store.network

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, ClusterSender, StoreMessage}
import org.wa9nnn.fdcluster.store.{JsonContainer, StoreSender}

import java.net.StandardSocketOptions._
import java.net.{InetSocketAddress, SocketException}
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class BroadcastListener @Inject()(config: Config, cluster: ClusterSender, store: StoreSender) extends LazyLogging {
  val socketTimeout: Duration = config.getDuration("fdcluster.broadcast.timeout")
  val port: Int = config.getInt("fdcluster.broadcast.port")
//  val messagesReceivedMeter: scala.Meter = metrics.meter("messagesReceived")
//  val receivedCounter: Gauge = Gauge.build
//    .name("BroadcastsReceived")
//    .help(s"Broadcast messages received on port: $port")
//    .register
//
//
//  val receivedBytes: Summary = Summary.build.name("broadcastSize").help("Request size in bytes.").register
//  val requestLatency: Summary = Summary.build.name("broadcastsPerMinute").help("broadcasts per minute.").register

  import java.nio.channels.DatagramChannel

  val channel: DatagramChannel = startServer()

  def startServer(): DatagramChannel = {
    val datagramChannel: DatagramChannel = DatagramChannel.open()
    try {
//      datagramChannel.setOption[java.lang.Boolean](SO_REUSEADDR, java.lang.Boolean.TRUE)
//      datagramChannel.setOption[java.lang.Boolean](SO_REUSEPORT, java.lang.Boolean.TRUE)
      datagramChannel.bind(new InetSocketAddress(port))
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    println(datagramChannel)
    datagramChannel
  }



  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    shutdown()
  }, "Broadcast Shutdown"))


  //  var buf = new Array[Byte](10000)

  def shutdown(): Unit = {
    continueListenerThread = false
    channel.close()
    logger.info("BroadcastListener shutdown.")
  }

  private var continueListenerThread = true
  private val packetCount = new AtomicInteger()

  new Thread(() => {
    do {
      try {
        //        val recv: DatagramPacket = new DatagramPacket(buf, buf.length)
        val byteBuffer = ByteBuffer.allocate(10000)
        val address = channel.receive(byteBuffer)
        val bytes = byteBuffer.array()
        for {
          jc <- JsonContainer(bytes)
          rec <- jc.received()
        } {
          packetCount.incrementAndGet()
          logger.trace(s"Broadcast packet received. sn: ${packetCount.incrementAndGet()}")
          logger.whenTraceEnabled {
            logger.trace(s"Got: $jc from  ${address}")
          }
          rec match {
            case sm: StoreMessage =>
              store ! sm
            case cm: ClusterMessage =>
              cluster ! cm
          }
        }
      } catch {
        case se: SocketException =>
          logger.error(s"Broadcast SocketException: ${se.getMessage}")
      }
    } while (continueListenerThread)
    logger.info("BroadcastListener thread finished.")

  }).start()
}
