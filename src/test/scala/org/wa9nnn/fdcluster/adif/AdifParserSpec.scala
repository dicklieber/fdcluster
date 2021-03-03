package org.wa9nnn.fdcluster.adif

import com.google.common.collect.Lists.StringAsImmutableList
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

import java.io.{StringBufferInputStream, StringReader}
import scala.io.{BufferedSource, Source}

class AdifParserSpec extends Specification with DataTables {

  "FiedCollector" >> {
    "input" || "predef" | "value" |
      "<EOR>" !! AdifResult.eor |
      "<PROGRAMID:11>MacLoggerDX" !! AdifEntry("PROGRAMID", "MacLoggerDX") |
      "<PROGRAMID:11>MacLoggerDX<" !! AdifEntry("PROGRAMID", "MacLoggerDX") |
      """line1
        |line2<PROGRAMID:11>MacLoggerDX<""".stripMargin !! AdifEntry("PROGRAMID", "MacLoggerDX") |
      "pedef<PROGRAMID:11>MacLoggerDX<" !! AdifEntry("PROGRAMID", "MacLoggerDX")|> { (input, thing) =>

      val source: Source = Source.fromString(input)
      val fc = new AdifParser(source)((entry: AdifResult) =>
        entry must beEqualTo (thing)
      )
      ok
    }
  }
}
