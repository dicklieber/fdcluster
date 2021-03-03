package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

import scala.io.Source

class AdifCollectorSpec extends Specification {

  "AdifCollectorSpec" should {
    "read" in {
      val url = getClass.getResource("/ARRL-FIELD-DAY.adi")
      val source = Source.fromURL(url)
      val adifFile = AdifCollector.read(source)
      adifFile.header must haveSize(4)
      adifFile.header(2).toString must beEqualTo ("""AdifEntry(PROGRAMID,N3FJP's ARRL Field Day Contest Log)""".stripMargin)
      adifFile.records must haveSize(54)
    }
  }
}
