
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.sync.{FdHour, NodeStatus, QsoHour, QsoHourIds}
import play.api.libs.json.{Format, Json}

object MessageFormats {

  import org.wa9nnn.fdlog.model.ModeJson.modeFormat

  implicit val fdHourFormat: Format[FdHour] = Json.format[FdHour]
  implicit val transmitterFormat: Format[OurStation] = Json.format[OurStation]
  implicit val bandModeFormat: Format[BandMode] = Json.format[BandMode]
  implicit val qsoFormat: Format[Qso] = Json.format[Qso]
  implicit val fdLogIdFormat: Format[FdLogId] = Json.format[FdLogId]
  implicit val qsoRecordFormat: Format[QsoRecord] = Json.format[QsoRecord]
  implicit val distributedQsoRecordFormat: Format[DistributedQsoRecord] = Json.format[DistributedQsoRecord]
  implicit val qsosFormat: Format[QsoHourIds] = Json.format[QsoHourIds]
  implicit val qsoPeriodFormat: Format[QsoHour] = Json.format[QsoHour]
  implicit val nodeStatsFormat: Format[NodeStatus] = Json.format[NodeStatus]
  type CallSign = String
  type Uuid = String
}
