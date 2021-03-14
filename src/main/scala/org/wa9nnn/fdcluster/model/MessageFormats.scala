
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
import org.wa9nnn.fdcluster.javafx.menu.{BuildLoadRequest, ImportRequest}

import java.net.URL
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, UuidsAtHost}
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHour, QsoHourDigest, QsoHourIds}
import org.wa9nnn.fdcluster.rig.{RigModel, RigSettings, SerialPortSettings}
import org.wa9nnn.fdcluster.store.JsonContainer
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.{Format, Json}

import scala.language.implicitConversions

/**
 * Creates [[play.api.libs.json.Format]] needed by Play JSon to parse and render JSON for case classes.
 * Usually includes with {{import org.wa9nnn.fdcluster.model.MessageFormats._}}
 * Which makes all implicits available when invoking [[Json.parse]] and [[Json.prettyPrint()]] or [[Json.toBytes()]].
 */
object MessageFormats {

  import UrlFormt.urlFormat

  implicit val fdHourFormat: Format[FdHour] = Json.format[FdHour]
  implicit val uuidsRequestFormat: Format[RequestUuidsForHour] = Json.format[RequestUuidsForHour]
  implicit val transmitterFormat: Format[OurStation] = Json.format[OurStation]
  implicit val bandFormat: Format[AvailableBand] = Json.format[AvailableBand]
  implicit val bandModeFormat: Format[BandModeOperator] = Json.format[BandModeOperator]
  implicit val qsoFormat: Format[Qso] = Json.format[Qso]
  implicit val nodeAddressFormat: Format[NodeAddress] = Json.format[NodeAddress]
  implicit val uuidsFormat: Format[UuidsAtHost] = Json.format[UuidsAtHost]
  implicit val fdLogIdFormat: Format[FdLogId] = Json.format[FdLogId]
  implicit val qsoRecordFormat: Format[QsoRecord] = Json.format[QsoRecord]
  implicit val distributedQsoRecordFormat: Format[DistributedQsoRecord] = Json.format[DistributedQsoRecord]
  implicit val qsosFormat: Format[QsoHourIds] = Json.format[QsoHourIds]
  implicit val qsosFromNodeFormat: Format[QsosFromNode] = Json.format[QsosFromNode]
  implicit val qsoHourDigestFormat: Format[QsoHourDigest] = Json.format[QsoHourDigest]
  implicit val qsoPeriodFormat: Format[QsoHour] = Json.format[QsoHour]
  implicit val nodeStatsFormat: Format[NodeStatus] = Json.format[NodeStatus]
  implicit val jsonContainerFormat: Format[JsonContainer] = Json.format[JsonContainer]

  implicit val rigPortSettingsFormat: Format[SerialPortSettings] = Json.format[SerialPortSettings]
  implicit val rigModelFormat: Format[RigModel] = Json.format[RigModel]
  implicit val rigSettingsFormat: Format[RigSettings] = Json.format[RigSettings]
  implicit val knownOperatorsFormat: Format[KnownOperators] = Json.format[KnownOperators]
  implicit val buildLoadRequestFormat: Format[BuildLoadRequest] = Json.format[BuildLoadRequest]
  implicit val importRequestFormat: Format[ImportRequest] = Json.format[ImportRequest]
  implicit val exportFileFormat: Format[ExportFile]= Json.format[ExportFile]
  implicit val exportRequestFormat: Format[AdifExportRequest] = Json.format[AdifExportRequest]
  implicit val cabriloFormat: Format[CabrilloValue] = Json.format[CabrilloValue]
  implicit val cabrilosFormat: Format[CabrilloValues] = Json.format[CabrilloValues]
  implicit val CabrilloExportRequestFormat: Format[CabrilloExportRequest] = Json.format[CabrilloExportRequest]

  type CallSign = String
  type Uuid = String
  type Digest = String

}

object TimeFormat {
  implicit def formatLocalDateTime(ldt: LocalDateTime): String = {
    ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
  }


}