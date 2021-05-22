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
import org.wa9nnn.fdcluster.model.sync.StoreMessage
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json

import java.time.Instant
import java.util.UUID


/**
 * This is what's in the store and journal.log.
 *
 * @param callSign    of the worked station.
 * @param exchange    from the worked station.
 * @param bandMode    that was used.
 * @param stamp       when QSO occurred.
 * @param uuid        id unique QSO id in time & space.
 * @param qsoMetadata info about ur station.
 */
case class Qso(callSign: CallSign,
               exchange: Exchange,
               bandMode: BandMode,
               qsoMetadata: QsoMetadata,
               stamp: Instant = Instant.now(),
               uuid: UUID = UUID.randomUUID
              ) extends Ordered[Qso] {
  def isDup(that: Qso): Boolean = {
    this.callSign == that.callSign &&
      this.bandMode == that.bandMode
  }

  lazy val display: String = s"$callSign on $bandMode in $fdHour"

  override def hashCode: Int = uuid.hashCode()


  override def compare(that: Qso): Int = this.callSign compareTo that.callSign

  lazy val fdHour: FdHour = {
    FdHour(stamp)
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

object Qso {
  def apply(callSign: CallSign,
            exchange: Exchange,
            bandMode: BandMode) (implicit qsoMetadata: QsoMetadata):Qso = {
    new Qso(callSign, exchange, bandMode, qsoMetadata)
  }
  def apply(json: String): Qso = {
    Json.parse(json).as[Qso]
  }
}


/**
 * This is what gets multi-casted to cluster.
 *
 * @param qso         the new QSO
 * @param nodeAddress where this came from.
 */
case class DistributedQso(qso: Qso, nodeAddress: NodeAddress) extends StoreMessage





