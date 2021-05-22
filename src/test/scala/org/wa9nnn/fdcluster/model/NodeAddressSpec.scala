package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification

class NodeAddressSpec extends Specification {
  val na0 = NodeAddress("10.10.10.0")
  val na1 = NodeAddress("10.10.10.1")
  val na1i1 = NodeAddress("10.10.10.1", instance = Option(1))

  "NodeAddress" should {
    "compareTo" in {
      na0 must beEqualTo(na0)
      na0 must not equalTo (na1)
      na1 must not equalTo (na1i1)
    }
    "display" >> {
      val na0 = NodeAddress("10.10.10.0")
      na0.display must beEqualTo("localhost (10.10.10.0)")
      na1i1.display must beEqualTo("localhost:1 (10.10.10.1)")
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
