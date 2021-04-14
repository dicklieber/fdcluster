package org.wa9nnn.util

import org.specs2.mutable.Specification

class CompressorSpec extends Specification {

  "CompressorSpec" >> {
    "round trip" >> {
      val nBytes = 10000
      val bytes: Array[Byte] = Array.tabulate(nBytes)(_.toByte)
      val str = Compressor(bytes)
      val length = str.length
      val avg = bytes.length / length
      val backAgain = Compressor(str).get
      backAgain(0) must beEqualTo(bytes(0))
      backAgain(999) must beEqualTo(bytes(999))
      backAgain.length must beEqualTo (nBytes)
      str must beEqualTo(
        """H4sIAAAAAAAAAGNgZGJmYWVj5+Dk4ubh5eMXEBQSFhEVE5eQlJKWkZWTV1BUUlZRVVPX0NTS1tHV
          |0zcwNDI2MTUzt7C0sraxtbN3cHRydnF1c/fw9PL28fXzDwgMCg4JDQuPiIyKjomNi09ITEpOSU1L
          |z8jMys7JzcsvKCwqLiktK6+orKquqa2rb2hsam5pbWvv6Ozq7unt658wcdLkKVOnTZ8xc9bsOXPn
          |zV+wcNHiJUuXLV+xctXqNWvXrd+wcdPmLVu3bd+xc9fuPXv37T9w8NDhI0ePHT9x8tTpM2fPnb9w
          |8dLlK1evXb9x89btO3fv3X/w8NHjJ0+fPX/x8tXrN2/fvf/w8dPnL1+/ff/x89fvP3///WcY9f+o
          |/0eA/wFB++N06AMAAA==
          |""".stripMargin)
    }
  }
}
