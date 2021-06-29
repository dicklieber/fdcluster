package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification

import java.net.URL

class NodeAddressSpec extends Specification {
  val na0 = NodeAddress(new URL("http", "10.10.10.0", 8081, ""))
  val na1 = NodeAddress(new URL("http", "10.10.10.1",8081, ""))
  val na1i1 = NodeAddress(new URL("http", "10.10.10.1", 8081,""), instance = Option(1))

  "NodeAddress" should {
    "compareTo" in {
      na0 must beEqualTo(na0)
      na0 must not equalTo (na1)
      na1 must not equalTo (na1i1)
    }
    "display" >> {
      val na0 = NodeAddress(new URL("http", "localhost", ""))
      na0.displayWithIp must beEqualTo("localhost  (127.0.0.1)")
      na1i1.displayWithIp must beEqualTo("10.10.10.1 ;1 (10.10.10.1)")
    }
    "uri" >> {
      na1i1.uri.toString() must beEqualTo ("//10.10.10.1:8081")
    }
    "instance" >> {
      na1i1.instance must beSome(1)
    }
    "url" >> {
      na1i1.url.toExternalForm must beEqualTo ("http://10.10.10.1:8081")
    }
  }
}
