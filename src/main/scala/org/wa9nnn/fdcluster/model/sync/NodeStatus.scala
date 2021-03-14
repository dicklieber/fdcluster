
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
 * @param nodeAddress       our IP and instance.
 * @param apiUrl            how to talk to this node. URL of the API.
 * @param qsoCount          of QSOs in db.
 * @param digest            over all QSO UUIDs
 * @param qsoHourDigests    for quickly determining what we have.
 * @param ourStation        band, mode, operator etc.
 * @param bandMode          band mode and current operator
 * @param qsoRate           qsos per minute
 * @param stamp             when this message was generated.
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

