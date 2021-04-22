package org.wa9nnn.fdcluster.store.network

import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.DistributedQsoRecord
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.JsonContainer
import org.wa9nnn.util.StructuredLogging
import play.api.libs.json.Json

object MessageDecoder extends StructuredLogging {
  def apply(jsonContainer: JsonContainer): Option[Any] = {
    val jsObject = Json.parse(jsonContainer.json)
    jsonContainer.className.split("""\.""").last match {
      case "DistributedQsoRecord" ⇒
        Some(jsObject.as[DistributedQsoRecord])
      case "NodeStatus" ⇒
        Some(jsObject.as[NodeStatus])
      case "Contest" =>
        Some(jsObject.as[Contest])
      case x ⇒
        logger.error(s"Unexpected JSON:$jsonContainer")
        None
    }
  }
}