
package org.wa9nnn.fdlog.model.sync

import java.net.{URI, URL}
import java.time.LocalDateTime

import akka.util.ByteString
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.Json

/**
 *
 * @param nodeAddress    our IP and instance.
 * @param apiUrl         how to talk to this node.
 * @param qsoCount          of QSOs in db.
 * @param digest         of all QSO UUIDs
 * @param qsoHourDigests for quickly determining what we have.
 * @param currentStation band, mode, operator etc.
 * @param qsoRate        qsos per minute
 * @param stamp          when this message was generated.
 */
case class NodeStatus(nodeAddress: NodeAddress,
                      apiUrl: URL,
                      qsoCount: Int,
                      digest: Digest,
                      qsoHourDigests: List[QsoHourDigest],
                      currentStation: CurrentStation,
                      qsoRate: Double,
                      stamp: LocalDateTime = LocalDateTime.now()) extends Codec {

  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    qsoHourDigests.find(_.startOfHour == fdHour)
  }

  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }
}

case class DigestAndCount(digest: Digest, count: Int)

