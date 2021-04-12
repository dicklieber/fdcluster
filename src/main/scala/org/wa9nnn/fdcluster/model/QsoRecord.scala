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

package org.wa9nnn.fdcluster.model

import akka.util.ByteString
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json
import MessageFormats._
import java.time.Instant
import java.util.UUID

/**
 * * One contact with another station.
 * * Things that are relevant for the contest plus a UUID.
 *
 * @param callSign of the worked station.
 * @param bandMode that was used.
 * @param exchange from the worked station.
 * @param stamp    when QSO occurred.
 * @param uuid     id unique QSO id in time & space.
 */
case class Qso(callSign: CallSign, bandMode: BandMode, exchange: Exchange, stamp: Instant = Instant.now(), uuid: String = UUID.randomUUID.toString) {
  def isDup(that: Qso): Boolean = {
    this.callSign == that.callSign &&
      this.bandMode == that.bandMode
  }
}


/**
 * This is what's in the store and journal.log.
 *
 * @param qso         info from worked station.
 * @param qsoMetadata info about ur station.
 */
case class QsoRecord(qso: Qso, qsoMetadata: QsoMetadata) extends Ordered[QsoRecord] {
  def callsign: CallSign = qso.callSign

  lazy val display: String = s"$callsign on ${qso.bandMode} in $fdHour"

  override def hashCode: Int = qso.uuid.hashCode()

  def dup(qso: Qso): Boolean = {
    this.qso.isDup(qso)
  }


  override def compare(that: QsoRecord): Int = this.callsign compareTo that.callsign

  lazy val fdHour: FdHour = {
    FdHour(qso.stamp)
  }

  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }

  def toJsonLine: String = {
    Json.toJson(this).toString()
  }

  def toJsonPretty: String = {
    Json.prettyPrint(Json.toJson(this))
  }
}

object QsoRecord {
  def apply(json: String): QsoRecord = {
    Json.parse(json).as[QsoRecord]
  }
}

case class QsosFromNode(nodeAddress: NodeAddress, qsos: List[QsoRecord]) {
  def size: Int = qsos.size

}

/**
 * This is what gets multi-casted to cluster.
 *
 * @param qsoRecord   the new QSO
 * @param nodeAddress where this came from.
 * @param size        number of QSOs in the database on this node. (Includes the new QSO)
 */
case class DistributedQsoRecord(qsoRecord: QsoRecord, nodeAddress: NodeAddress, size: Int)

object DistributedQsoRecord {
  val qsoVersion = "1:"

}




