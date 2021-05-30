package org.wa9nnn.fdcluster.store.network

import com.typesafe.scalalogging.LazyLogging

import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket}
import java.util.{Timer, TimerTask}

object NetworkTest extends App {
  val multicastAddress = InetAddress.getByName("239.73.88.0")
  private val multicast = new Multicast(multicastAddress, 1174)
  private val broadcast = new Broadcast(1175)

  val timer = new Timer("PropertyCellTimer", true)
  var sn = 0
  timer.scheduleAtFixedRate(new TimerTask {
    override def run(): Unit = {
      multicast.send(sn)
      broadcast.send(sn)
      sn += 1
    }
  }, 10, 1000)

}

class Broadcast( port: Int) extends LazyLogging {
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
      logger.info(s"Broadcast: addr:${recv.getAddress} message: $sData")
    } while (true)

  }).start()


  def send(sn: Int): Unit = {
    val message = s"broadcast Message>: $sn".getBytes()
    val datagramPacket = new DatagramPacket(message, message.length, address, port)
    socket.send(datagramPacket)
  }
}
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
      logger.info(s"Multicast: addr:${recv.getAddress} message: $sData")
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