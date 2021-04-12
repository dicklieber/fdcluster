package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification

import java.util.UUID

class UuidUtilsSpec extends Specification {

  "UuidUtilsSpec" should {
    "round trip" in {
      val uuid = UUID.randomUUID()
      val bytes = UuidUtils.asBytes(uuid)
      val backAgain = UuidUtils.asUuid(bytes)
      backAgain must beEqualTo (uuid)
      bytes must haveLength(16)
    }

  }
}
