package org.wa9nnn.fdcluster.store.network

import com.typesafe.scalalogging.LazyLogging

import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket}
import java.util.{Timer, TimerTask}
import scala.jdk.CollectionConverters.MapHasAsScala

object NetworkTest extends App with LazyLogging {
  System.getProperties.asScala.foreach{case(k,v) =>
    println(s"$k: \t$v")
  }

  private val localHost = InetAddress.getLocalHost
  val multicastAddress = InetAddress.getByName("239.73.88.0")
  logger.info(s"NetworkTest on ${localHost.getHostName}")
  val timer = new Timer("PropertyCellTimer", true)
  private val multicast = new Multicast(multicastAddress, 1174)
//  private val broadcast = new Broadcast(1121)
  var sn = 0
  timer.scheduleAtFixedRate(new TimerTask {
    override def run(): Unit = {
      multicast.send(sn)
//      broadcast.send(sn)
      sn += 1
    }
  }, 10, 1000)

  class Multicast(multicastGroup: InetAddress, port: Int) extends LazyLogging {
    var multicastSocket: MulticastSocket = new MulticastSocket(port);
    multicastSocket.setReuseAddress(true)
    multicastSocket.joinGroup(multicastGroup);

    var buf = new Array[Byte](1000);

    new Thread(() => {
      logger.info(s"Listening on ${multicastGroup.getHostName}:$port")
      do {
        val recv: DatagramPacket = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(recv);
        val data: Array[Byte] = recv.getData
        val sData = new String(data)
//        if (recv.getAddress != localHost)
          logger.info(s"Multicast: addr:${recv.getAddress} => ${localHost.getHostName}:${recv.getPort} message: $sData")
      } while (true)

    }).start()


    def send(sn: Int): Unit = {
      val message = s"multicast Message: $sn".getBytes()
      val datagramPacket = new DatagramPacket(message, message.length, multicastGroup, port)
      multicastSocket.send(datagramPacket)
    }

    /*
                                              String ipAddress, int port) throws IOException {
              DatagramSocket socket = new DatagramSocket();
              InetAddress group = InetAddress.getByName(ipAddress);
              byte[] msg = message.getBytes();
              DatagramPacket packet = new DatagramPacket(msg, msg.length,
                      group, port);
              socket.send(packet);
              socket.close();

     */
  }

  class Broadcast(port: Int) extends LazyLogging {
    val address = InetAddress.getByName("255.255.255.255")
    val socket = new DatagramSocket(port)

    var buf = new Array[Byte](1000);

    new Thread(() => {
      logger.info(s"Listening on ${address.getHostName}:$port")
      do {
        val recv: DatagramPacket = new DatagramPacket(buf, buf.length);
        socket.receive(recv);
        val data: Array[Byte] = recv.getData
        val sData = new String(data)
//        if (recv.getAddress != localHost)
          logger.info(s"Broadcast: addr:${recv.getAddress} => ${localHost.getHostName} message: $sData")
      } while (true)

    }).start()


    def send(sn: Int): Unit = {
      val message = s"broadcast Message>: $sn".getBytes()
      val datagramPacket = new DatagramPacket(message, message.length, address, port)
      socket.send(datagramPacket)
    }
  }
}


