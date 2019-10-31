
package org.wa9nnn.fdcluster.store.network

import java.net.{DatagramPacket, InetAddress, MulticastSocket}

object UdpMulticastListner {

  def main(args: Array[String]): Unit = {
    println("UdpMulticastListner")


    var msg = "Hello";
    var group = InetAddress.getByName("239.73.88.0");
    val port = 1174
    var s = new MulticastSocket(port);
    s.joinGroup(group);
//    var hi = new DatagramPacket(msg.getBytes(), msg.length(),
//      group, port);
//    s.send(hi);
    // get their responses!
    var buf = new Array[Byte](1000);
    var recv = new DatagramPacket(buf, buf.length);
    s.receive(recv);

    println(s"Got: ${recv.getAddress} ")
    // OK, I'm done talking - leave the group...
    s.leaveGroup(group);

  }
}
