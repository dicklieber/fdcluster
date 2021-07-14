package org.wa9nnn.fdcluster.store.network

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.model.sync.{ClusterMessage, ClusterSender, StoreMessage}
import org.wa9nnn.fdcluster.store.{JsonContainer, StoreSender}

import java.net.StandardSocketOptions._
import java.net.{InetSocketAddress, SocketException}
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class BroadcastListener @Inject()(config: Config, cluster: ClusterSender, store: StoreSender) extends LazyLogging with DefaultInstrumented {
  val socketTimeout: Duration = config.getDuration("fdcluster.broadcast.timeout")
  val port: Int = config.getInt("fdcluster.broadcast.port")
  val messagesReceivedMeter: scala.Meter = metrics.meter("messagesReceived")


  import java.nio.channels.DatagramChannel

  val channel: DatagramChannel = startServer()

  def startServer(): DatagramChannel = {
    val datagramChannel: DatagramChannel = DatagramChannel.open()
    try {
      datagramChannel.setOption[java.lang.Boolean](SO_REUSEADDR, java.lang.Boolean.TRUE)
      datagramChannel.setOption[java.lang.Boolean](SO_REUSEPORT, java.lang.Boolean.TRUE)
      datagramChannel.bind(new InetSocketAddress(port))
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    println(datagramChannel)
    datagramChannel
  }

  //  val t: Boolean = true
  //  private val socket: DatagramSocket = new DatagramSocket()
  //    .setOption[java.lang.Boolean](SO_REUSEPORT, java.lang.Boolean.TRUE)
  //    .setOption[java.lang.Boolean](SO_REUSEADDR, t)
  //  //  socket.bind(new InetSocketAddress(port))

  //  private val reuseAddress: Boolean = socket.getReuseAddress
  //  private val b: Boolean = socket.getBroadcast
  //  private val localSocketAddress: SocketAddress = socket.getLocalSocketAddress
  //  private val localPort: Int = socket.getLocalPort
  //  private val port1: Int = socket.getPort
  //  private val reuseAddr: lang.Boolean = socket.getOption(SO_REUSEADDR)
  //  private val resusePort: lang.Boolean = socket.getOption(SO_REUSEPORT)

  //  private val socket2: DatagramSocket = new DatagramSocket()
  //    .setOption[java.lang.Boolean](SO_REUSEPORT, java.lang.Boolean.TRUE)
  //    .setOption[java.lang.Boolean](SO_REUSEADDR, t)
  //  socket.bind(new InetSocketAddress(port))
  //  private val reuseAddr2: lang.Boolean = socket2.getOption(SO_REUSEADDR)
  //  private val resusePort2: lang.Boolean = socket2.getOption(SO_REUSEPORT)


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

        for {
          jc <- JsonContainer(byteBuffer.array())
          rec <- jc.received()
        } {
          packetCount.incrementAndGet()
          logger.trace(s"Broadcast packet received. sn: ${packetCount.incrementAndGet()}")
          messagesReceivedMeter.mark()
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
