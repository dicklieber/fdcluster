
package org.wa9nnn.fdcluster.store.network

import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.{DatagramPacket, InetAddress, MulticastSocket}

object UdpMulticastListner {

  def main(args: Array[String]): Unit = {
    println("UdpMulticastListner")


    var msg = "Hello";
    var group = InetAddress.getByName("239.73.88.0");
    val port = 1174
    var s: MulticastSocket = new MulticastSocket(port);
    s.setReuseAddress(true)
    s.joinGroup(group);
//    var hi = new DatagramPacket(msg.getBytes(), msg.length(),
//      group, port);
//    s.send(hi);
    // get their responses!
    var buf = new Array[Byte](1000);

    do {
      val recv: DatagramPacket = new DatagramPacket(buf, buf.length);
      s.receive(recv);
      val data: Array[Byte] = recv.getData
      val sData = new String(data)
      println(s"Got: ${sData.size} from  ${recv.getAddress} ")

      val jsonContainer = JsonContainer(sData)
      println(s"Got: $jsonContainer} ")
    } while (true)
    // OK, I'm done talking - leave the group...
    s.leaveGroup(group);

  }
}
