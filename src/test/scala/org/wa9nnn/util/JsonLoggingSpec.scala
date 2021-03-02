package org.wa9nnn.util

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import play.api.libs.json.{Format, Json}

import java.util.UUID

/**
 * Crappy Guru tests, a guru should look at log output
 */
class JsonLoggingSpec extends Specification with JsonLogging {
  case class SomeClass(hello: String = "World", goodbye: String = "covid")

  implicit val f: Format[SomeClass] = Json.format[SomeClass]

  "JsonLoggingSpec" should {
    "logJson simple" in {
      val someClass = new SomeClass()
      logJson("someClass", someClass)
      ok
    }
    "logJson nested" in {
      val fdLOgId = FdLogId(nodeAddress = NodeAddress(0, "10.10.10.1"), uuid = UUID.randomUUID().toString)

      val ourStation = new QsoRecord(Qso("WA9NNN", BandModeOperator(), new Exchange()), Contest(year = 2021), OurStation(), fdLOgId)
      logJson("ourStation", ourStation)
      ok
    }
  }
}
