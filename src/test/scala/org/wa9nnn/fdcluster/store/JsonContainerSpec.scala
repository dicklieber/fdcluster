package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.MessageFormats._

import scala.util.Try

class JsonContainerSpec extends Specification {

  "JsonContainer" >> {
    "happy" >> {
      val someclass = Contest()
      val container = JsonContainer(someclass)
      container.className must beEqualTo("org.wa9nnn.fdcluster.contest.Contest")
      val bytes = container.bytes

      val backAgain: Try[JsonContainer] = JsonContainer(bytes)
      backAgain must beSuccessfulTry(container)

      val jc: JsonContainer = backAgain.get
      val contentsBackAgain: Option[Any] = jc.received()
      contentsBackAgain must beSome(someclass)
    }
  }
}
