package org.wa9nnn.fdcluster.javafx.sync

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json
import org.wa9nnn.fdcluster.model.MessageFormats._

class TransactionIdSpec extends Specification {
  val nodeAddress0 = NodeAddress()
  val nodeAddress1 = NodeAddress(instance = 1)
  val fdHour = FdHour(15, 0)
  val transactionId = TransactionId(nodeAddress0, nodeAddress1, fdHour, getClass)
"TransactionId" >> {
  "round trip" >> {
    val value1 = Json.toJson(transactionId)
    val sJson = Json.prettyPrint(value1)

    val backAgain = Json.parse(sJson).as[TransactionId]
    backAgain must beEqualTo (transactionId)
  }
}
}
