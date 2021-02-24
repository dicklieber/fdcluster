package org.wa9nnn.fdcluster.adif

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class FieldCollectorSpec extends Specification with DataTables {

  "FiedCollector" >> {
    "input" || "predef" | "value" |
      "<EOR>" !! Entry("" , "<EOR>" , "") |
      "<PROGRAMID:11>MacLoggerDX" !! Entry("" , "<PROGRAMID:11>" , "MacLoggerDX") |
      "<PROGRAMID:11>MacLoggerDX<" !! Entry("" , "<PROGRAMID:11>" , "MacLoggerDX") |
      """line1
        |line2<PROGRAMID:11>MacLoggerDX<""".stripMargin !! Entry(
        """line1
          |line2""".stripMargin , "<PROGRAMID:11>" , "MacLoggerDX") |
      "pedef<PROGRAMID:11>MacLoggerDX<" !! Entry("pedef", "<PROGRAMID:11>" , "MacLoggerDX" )|> { (input, thing) =>
      val fc = new FieldCollector((entry: Entry) =>
        entry must beEqualTo (thing)
      )
      input.foreach(fc(_))
      ok
    }
  }
}
