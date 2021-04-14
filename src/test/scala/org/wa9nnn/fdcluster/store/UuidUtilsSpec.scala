package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification
import org.wa9nnn.util.UuidUtil

import java.util.UUID

class UuidUtilsSpec extends Specification {

  "UuidUtilsSpec" should {
    "round trip" in {
      val uuid = UUID.randomUUID()
      val bytes = UuidUtil.apply(uuid)
      val backAgain = UuidUtil.apply(bytes)
      backAgain must beEqualTo (uuid)
      bytes must haveLength(16)
    }

  }
}
