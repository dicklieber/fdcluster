
package org.wa9nnn.fdcluster.model.sync

import akka.util.ByteString
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{BandModeOperator, Codec, NodeAddress, OurStation}
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json

import java.net.URL
import java.time.LocalDateTime
/**
 *
 * @param nodeAddress    our IP and instance.
 * @param apiUrl         how to talk to this node.
 * @param qsoCount          of QSOs in db.
 * @param digest         of all QSO UUIDs
 * @param qsoHourDigests for quickly determining what we have.
 * @param ourStation band, mode, operator etc.
 * @param qsoRate        qsos per minute
 * @param stamp          when this message was generated.
 */
case class NodeStatus(nodeAddress: NodeAddress,
                      apiUrl: URL,
                      qsoCount: Int,
                      digest: Digest,
                      qsoHourDigests: List[QsoHourDigest],
                      ourStation: OurStation,
                      bandMode: BandModeOperator,
                      qsoRate: Double,
                      stamp: LocalDateTime = LocalDateTime.now()) extends Codec {
assert(bandMode != null, "null BandModeOperator")
  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    qsoHourDigests.find(_.startOfHour == fdHour)
  }

  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }
}

case class DigestAndCount(digest: Digest, count: Int)

