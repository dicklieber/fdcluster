package org.wa9nnn.fdcluster.javafx.sync

import java.net.URL
import java.time.LocalDateTime
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json

import java.util.UUID

class RequestUuidsForHourSpec extends Specification {

  "UuidRequestSpec" should {
    "UuidRequest" in {

      val url = new URL("http://127.0.0.0")
      val uuidRequest = RequestUuidsForHour(url)
      val jsValue = Json.toJson(uuidRequest)
      val str = jsValue.toString()

      val date = LocalDateTime.now().toLocalDate
      val fdh1 = FdHour( 4)
      val fdh2 = FdHour( 23)
      val fdhours = List(fdh1, fdh2)
      val uuidRequest1 = RequestUuidsForHour(url, fdhours)
      val jsValue1 = Json.toJson(uuidRequest1)
      val str1 = jsValue1.toString()


      val uuids = UuidsAtHost(NodeAddress(), List(UUID.randomUUID()))
      val res = Json.toJson(uuids)
      val ess = res.toString()
      ess must beEqualTo ("{\"nodeAddress\":{\"host\":\"localhost\",\"instance\":0,\"httpPort\":8000},\"uuids\":[\"111\"]}")
    }

  }
}
