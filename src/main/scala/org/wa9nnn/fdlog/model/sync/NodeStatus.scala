
package org.wa9nnn.fdlog.model.sync

import java.time.LocalDateTime

import akka.util.ByteString
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.Json

/**
 *
 * @param nodeAddress    our IP and instance.
 * @param count          of QSOs in db.
 * @param qsoHourDigests for quickly determining what we have.
 * @param currentStation band, mode, operator etc.
 * @param stamp          when this message was generated.
 */
case class NodeStatus(nodeAddress: NodeAddress,
                      count: Int,
                      qsoHourDigests: List[QsoHourDigest],
                      currentStation: CurrentStation,
                      stamp: LocalDateTime = LocalDateTime.now()) extends Codec {

  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    qsoHourDigests.find(_.startOfHour == fdHour)
  }

  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }
}

case class DigestAndCount(digest: Digest, count: Int)

