package org.wa9nnn.fdcluster.cabrillo

import com.sun.javafx.application.PlatformImpl
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.{BeforeAfter, Specification}
import scalafx.application.Platform

class CabrilloFieldsParserSpec extends Specification with BeforeAfter {

  "CabrilloFieldsParserSpec" should {
    "combo parse" >> {
      val CabrilloFieldsSource.combo(n, c, ch) = """Combo: Mode CATEGORY-MODE  [$modes]"""
      n must beEqualTo("Mode")
      c must beEqualTo("CATEGORY-MODE")
      ch must beEqualTo("$modes")
    }
    "text parse" >> {
      val CabrilloFieldsSource.text(n, c) = """Text: City ADDRESS-CITY"""
      n must beEqualTo("City")
      c must beEqualTo("ADDRESS-CITY")
    }
    "textarea  parse" >> {
      val CabrilloFieldsSource.textArea(n, c) = """TextArea: Address ADDRESS"""
      n must beEqualTo("Address")
      c must beEqualTo("ADDRESS")
    }

//    "parseFields" in {
//      implicit val savedValues: CabrilloValues = new CabrilloValues()
//      val config = ConfigFactory.load()
//      val source = new CabrilloFieldsSource(config)
//      val f = source.cabrilloFields
//      f must haveSize(15)
//    }
  }

  override def before: Any = {
    // We need to have saclafx running becuse the
    // control creation need it to work.
    Platform.startup(new Runnable {
      override def run(): Unit = {
        // don't need to actuall run anytin, but javafx gets started/
      }
    })

  }

  override def after: Any = {
    PlatformImpl.exit()
  }
}
