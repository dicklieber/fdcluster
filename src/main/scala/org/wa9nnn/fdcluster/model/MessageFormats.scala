
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

import org.wa9nnn.fdcluster.cabrillo.{CabrilloExportRequest, CabrilloValue, CabrilloValues}
import org.wa9nnn.fdcluster.contest.{Contest, Journal, JournalHeader}
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.javafx.menu.{BuildLoadRequest, ImportRequest}
import org.wa9nnn.fdcluster.javafx.sync._
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHour, QsoHourDigest, QsoHourIds}
import org.wa9nnn.fdcluster.rig.{RigModel, RigSettings, SerialPortSettings}
import org.wa9nnn.fdcluster.store.JsonContainer
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.util.UuidUtil.uuidFormat._
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.UUID
import scala.language.implicitConversions
/**
 * Creates [[play.api.libs.json.Format]] needed by Play JSon to parse and render JSON for case classes.
 * Usually includes with {{import org.wa9nnn.fdcluster.model.MessageFormats._}}
 * Which makes all implicits available when invoking [[Json.parse]] and [[Json.prettyPrint()]] or [[Json.toBytes()]].
 */
object MessageFormats {

  val builder = Array.newBuilder[Format[_]]
  def c[T](f: Format[T]): Format[T] = {
    val str: Node = f.toString
    builder += f
    f
  }

  implicit val entcFromat: Format[EntryCategory] = c(Json.format[EntryCategory])
  implicit val fdcFromat: Format[FdClass] = Json.format[FdClass]
  implicit val sectFromat: Format[Section] = Json.format[Section]
//  implicit val excFromat: Format[Exchange] = Json.format[Exchange]
  implicit val fdHourFormat: Format[FdHour] = Json.format[FdHour]
  implicit val nodeAddressFormat: Format[NodeAddress] = Json.format[NodeAddress]
  implicit val stepFormat: Format[Step] = Json.format[Step]
  implicit val tidFormat: Format[TransactionId] = Json.format[TransactionId]
  implicit val rqfhFormat: Format[RequestQsosForHour] = Json.format[RequestQsosForHour]
  implicit val rqfuFormat: Format[RequestQsosForUuids] = Json.format[RequestQsosForUuids]
  implicit val uuidsRequestFormat: Format[RequestUuidsForHour] = Json.format[RequestUuidsForHour]
  implicit val cf: Format[Contest] = Json.format[Contest]
  implicit val bandFormat: Format[AvailableBand] = Json.format[AvailableBand]
//  implicit val bandModeFormat: Format[BandMode] = Json.format[BandMode]
  implicit val bandModeOpFormat: Format[CurrentStation] = Json.format[CurrentStation]
  implicit val qsoFormat: Format[Qso] = Json.format[Qso]
  implicit val uuidsFormat: Format[UuidsAtHost] = Json.format[UuidsAtHost]
  implicit val qmd: Format[QsoMetadata] = Json.format[QsoMetadata]
  implicit val qsoRecordFormat: Format[QsoRecord] = Json.format[QsoRecord]
  implicit val distributedQsoRecordFormat: Format[DistributedQsoRecord] = Json.format[DistributedQsoRecord]
  implicit val qsosFormat: Format[QsoHourIds] = Json.format[QsoHourIds]
  implicit val qsosFromNodeFormat: Format[QsosFromNode] = Json.format[QsosFromNode]
  implicit val qsoHourDigestFormat: Format[QsoHourDigest] = Json.format[QsoHourDigest]
  implicit val qsoPeriodFormat: Format[QsoHour] = Json.format[QsoHour]
  implicit val jFormat: Format[Journal] = Json.format[Journal]
  implicit val nodeStatsFormat: Format[NodeStatus] = Json.format[NodeStatus]
  implicit val jsonContainerFormat: Format[JsonContainer] = Json.format[JsonContainer]
  implicit val jhFormat: Format[JournalHeader] = Json.format[JournalHeader]
  implicit val rigPortSettingsFormat: Format[SerialPortSettings] = Json.format[SerialPortSettings]
  implicit val rigModelFormat: Format[RigModel] = Json.format[RigModel]
  implicit val rigSettingsFormat: Format[RigSettings] = Json.format[RigSettings]
  implicit val knownOperatorsFormat: Format[KnownOperators] = Json.format[KnownOperators]
  implicit val buildLoadRequestFormat: Format[BuildLoadRequest] = Json.format[BuildLoadRequest]
  implicit val importRequestFormat: Format[ImportRequest] = Json.format[ImportRequest]
  implicit val exportFileFormat: Format[ExportFile] = Json.format[ExportFile]
  implicit val exportRequestFormat: Format[AdifExportRequest] = Json.format[AdifExportRequest]
  implicit val cabriloFormat: Format[CabrilloValue] = Json.format[CabrilloValue]
  implicit val cabrilosFormat: Format[CabrilloValues] = Json.format[CabrilloValues]
  implicit val CabrilloExportRequestFormat: Format[CabrilloExportRequest] = Json.format[CabrilloExportRequest]


  type CallSign = String
  type Uuid = UUID
  type Digest = String
  type Node = String

}

object TimeFormat {
  implicit def formatLocalDateTime(ldt: LocalDateTime): String = {
    ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
  }


}