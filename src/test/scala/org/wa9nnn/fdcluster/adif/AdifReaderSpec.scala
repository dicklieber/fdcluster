package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

import java.net.URL
import scala.io.BufferedSource

class AdifReaderSpec extends Specification {

  "AdifReaderSpec" >> {
    "read" >> {
      val in = getClass.getResourceAsStream("/MacLoggerDX_Export.adi")
      val bs = new BufferedSource(in)
      val r: AdifFile = AdifCollector.read(bs)
      r.header must haveSize(3)
      r.records must haveSize(2)
      ok
    }
  }
}


object AdifReader extends App {
  val url =  new URL("file", "", "/Users/dlieber/MacLoggerDX_ExportBig.adi")


  private val source = new BufferedSource(url.openStream())
  val a =  AdifCollector.read(source, Some(url))
  println(a)
}