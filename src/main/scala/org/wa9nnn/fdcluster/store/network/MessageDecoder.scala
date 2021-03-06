package org.wa9nnn.fdcluster.store.network

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.DistributedQso
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.sync.{HeartBeatMessage, NodeStatus}
import org.wa9nnn.fdcluster.store.JsonContainer
import play.api.libs.json.Json

object MessageDecoder extends LazyLogging {
  def apply(jsonContainer: JsonContainer): Option[Any] = {
    val jsObject = Json.parse(jsonContainer.json)
    jsonContainer.className.split("""\.""").last match {
      case "DistributedQso" ⇒
        Some(jsObject.as[DistributedQso])
      case "NodeStatus" ⇒
        Some(jsObject.as[NodeStatus])
      case "Contest" =>
        Some(jsObject.as[Contest])
      case "HeartBeatMessage" =>
        Some(jsObject.as[HeartBeatMessage])

      case x ⇒
        logger.error(s"Unexpected JSON:$jsonContainer")
        None
    }
  }
}
