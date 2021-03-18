package org.wa9nnn.util

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import play.api.libs.json.{Format, Json}

import java.net.InetAddress

/**
 * Crappy Guru tests, a guru should look at log output
 */
class JsonLoggingSpec extends Specification with StructuredLogging {
  case class SomeClass(hello: String = "World", goodbye: String = "covid")

  implicit val f: Format[SomeClass] = Json.format[SomeClass]

  "JsonLogging" should {
    "logJson simple" in {
      val someClass = SomeClass()
      logJson("someClass", someClass)
      ok
    }
  }
}
