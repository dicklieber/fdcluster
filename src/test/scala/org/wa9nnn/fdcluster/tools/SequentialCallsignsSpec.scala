package org.wa9nnn.fdcluster.tools

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.CallSign
import org.wa9nnn.fdcluster.tools.SequentialChar._

class SequentialCallsignsSpec extends Specification {
  "SequentialCallsigns" should {
    val callsignGenerator = new SequentialCallsigns
    "next" in {
      println("SequentialCallsigns")
      val setBuilder = Set.newBuilder[CallSign]
      for (_ <- 0 until 100) {
        val next = callsignGenerator.next()
        setBuilder += next
      }
      setBuilder.result must haveLength(100)
    }
  }

}

class SequentialCharSpec extends Specification {
  "SequentialChar" >> {
    "single char" >> {
      val sequentialChar = SequentialChar("ABC")
      sequentialChar.next must beEqualTo ("A")
      sequentialChar.next must beEqualTo ("B")
      sequentialChar.next must beEqualTo ("C")
      sequentialChar.next must beEqualTo ("A")
    }
    "two chars" >> {
      val sequentialChar = SequentialChar("AB", SequentialChar("123"))

      sequentialChar.next must beEqualTo ("1A")
      sequentialChar.next must beEqualTo ("1B")
      sequentialChar.next must beEqualTo ("2A")
      sequentialChar.next must beEqualTo ("2B")
      sequentialChar.next must beEqualTo ("3A")
      sequentialChar.next must beEqualTo ("3B")
      sequentialChar.next must beEqualTo ("1A")
      sequentialChar.next must beEqualTo ("1B")
    }
  }
}