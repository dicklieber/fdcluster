package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

import scala.io.Source

class AdifCollectorSpec extends Specification {

  "AdifCollectorSpec" should {
    "read an N3FJP adif" in {
      // store exchanfge in <Class:2> and <ARRL_Sect:2>
      val url = getClass.getResource("/ARRL-FIELD-DAY.adi")
      val source = Source.fromURL(url)
      val adifFile = AdifCollector.read(source)
      adifFile.header must haveSize(4)
      adifFile.header(2).toString must beEqualTo ("""AdifEntry(PROGRAMID,N3FJP's ARRL Field Day Contest Log)""".stripMargin)
      adifFile.records must haveSize(54)
    }
    "read a MacloggerDX adif" in {
      // sgtored exchange in <srx_string:5>
      val url = getClass.getResource("/MacLoggerDX_Export.adi")
      val source = Source.fromURL(url)
      val adifFile = AdifCollector.read(source)
      adifFile.header must haveSize(3)
      adifFile.header(2).toString must beEqualTo ("""AdifEntry(ADIF_VER,3.0.7)""".stripMargin)
      adifFile.records must haveSize(2)
    }
  }
}
