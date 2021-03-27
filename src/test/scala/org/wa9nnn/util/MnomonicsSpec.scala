package org.wa9nnn.util

import org.specs2.mutable.Specification

class MnomonicsSpec extends Specification {

  "Mnomonics" should {
    "toMnemonic" in {

      Mnomonics("1h IL") must beEqualTo ("1 Hotel India Lima")
    }
  }
}
