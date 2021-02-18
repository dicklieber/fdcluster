
package org.wa9nnn.fdcluster.model

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
  implicit val bandModeFormat: Format[BandMode] = Json.format[BandMode]
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

  type CallSign = String
  type Uuid = String
  type Digest = String

}

object TimeFormat {
  implicit def formatLocalDateTime(ldt: LocalDateTime): String = {
    ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
  }


}