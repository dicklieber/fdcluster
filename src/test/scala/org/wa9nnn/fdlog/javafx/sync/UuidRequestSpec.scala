package org.wa9nnn.fdlog.javafx.sync

import java.time.LocalDateTime

import com.sun.javafx.scene.NodeHelper.NodeAccessor
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.store.network.FdHour

class UuidRequestSpec extends Specification {

  "UuidRequestSpec" should {
    "UuidRequest" in {

      val uuidRequest = UuidRequest()
      val jsValue = Json.toJson(uuidRequest)
      val str = jsValue.toString()

      val date = LocalDateTime.now().toLocalDate
      val fdh1 = FdHour(date, 4)
      val fdh2 = FdHour(date, 23)
      val fdhours = List(fdh1, fdh2)
      val uuidRequest1 = UuidRequest(fdhours)
      val jsValue1 = Json.toJson(uuidRequest1)
      val str1 = jsValue1.toString()


      val uuids = UuidsAtHost(NodeAddress(), List("111"), uuidRequest1)
      val res = Json.toJson(uuids)
      val ess = res.toString()
      ok
    }

  }
}
