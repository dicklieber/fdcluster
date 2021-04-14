package org.wa9nnn.util

import org.specs2.mutable.Specification

import java.util.UUID

class UuidBinarySerializerSpec extends Specification {

  "UuidBinarySerializer" >> {
    "apply" >> {
      val uuid1 = UUID.fromString("371af7e7-65f9-44f8-a74b-3294488c0cb0")
      val uuid2 = UUID.fromString("fdd3e679-f65e-45f9-8e49-81d90b4c6f7b")
      val uuid3= UUID.fromString("564ff2be-d2ec-40bf-9053-d5078af47ebb")
      val in = Seq(uuid1, uuid2, uuid3)
      val bytes = UuidBinarySerializer[UUID](in) { u =>
        u
      }
      bytes.size / 16 must beEqualTo (3)
      val result = bytes.map(b => f"$b%02x").grouped(16).map(_.mkString(" ")).mkString("\n")
      result must beEqualTo ("""37 1a f7 e7 65 f9 44 f8 a7 4b 32 94 48 8c 0c b0
                               |fd d3 e6 79 f6 5e 45 f9 8e 49 81 d9 0b 4c 6f 7b
                               |56 4f f2 be d2 ec 40 bf 90 53 d5 07 8a f4 7e bb""".stripMargin)
    }
  }
}
