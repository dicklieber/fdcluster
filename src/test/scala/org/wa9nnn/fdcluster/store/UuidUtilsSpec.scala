package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification
import org.wa9nnn.util.UuidUtil

import java.util.UUID

class UuidUtilsSpec extends Specification {

  "UuidUtilsSpec" should {
    "bytes round trip" in {
      val uuid = UUID.randomUUID()
      val bytes = UuidUtil.apply(uuid)
      val backAgain = UuidUtil.apply(bytes)
      backAgain must beEqualTo (uuid)
      bytes must haveLength(16)
    }
    "base64 round trip" >> {
      val uuid = UUID.randomUUID()
      val b64 = UuidUtil.toBase64(uuid)
      val backAgain = UuidUtil.fromBase64(b64)
      backAgain must beEqualTo (uuid)

      val nativeString = uuid.toString
      val diff = nativeString.length- b64.length
      println(s"UuidUtil base64 saved $diff bytes!")
      b64.length must beLessThan(nativeString.length)
    }

  }
}
