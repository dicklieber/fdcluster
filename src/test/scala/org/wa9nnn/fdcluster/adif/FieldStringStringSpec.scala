package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

class FieldStringStringSpec extends Specification {

  "FieldStringString" should {
    "value" in {
      val f = new AString("CALL", "W9XB")
      f.name must beEqualTo ("CALL")
      f.value must beEqualTo ("W9XB")
      f.dataTypeIndicator must beEqualTo ("S")
    }
  }
}
