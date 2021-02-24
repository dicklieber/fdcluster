package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

import scala.io.BufferedSource

class AdifReaderSpec extends Specification {

  "AdifReaderSpec" >> {
    "read" >> {
      val in = getClass.getResourceAsStream("/MacLoggerDX_Export.adi")
      val bs = new BufferedSource(in)
      val r: List[Entry] = AdifReader.read(bs)
      r.foreach(println(_))
      r.head.toString.filterNot(_ == '\r') must beEqualTo("""This ADIF file was created by MacLoggerDX
                                       |another line<PROGRAMID:11>MacLoggerDX""".stripMargin)
      r.last.toString must beEqualTo("<EOR>")
      ok
    }
  }
}
