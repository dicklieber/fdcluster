package org.wa9nnn.fdlog.model

import java.time.Instant
import java.util.UUID

import org.wa9nnn.fdlog.model.Contact.CallSign
import play.api.libs.json.{Format, Json}

/**
  *
  * @param contest     ARRL winter and year
  * @param ourStation    within our site.
  * @param qso         who we worked.
  * @param fdLogId     housekeeping info for replication.
  */
case class QsoRecord(contest: Contest,
                     ourStation: OurStation,
                     qso: Qso,
                     fdLogId: FdLogId) extends Ordered[QsoRecord] {
  def callsign: CallSign = qso.callsign

  def uuid: UUID = fdLogId.uuid


  override def hashCode: Int = fdLogId.uuid.hashCode()

  def dup(qso: Qso): Boolean = {
    this.qso.isDup(qso)
  }

  override def compare(that: QsoRecord): Int = this.callsign compareTo that.callsign
}


/**
  * One contact with another station.
  *
  */
case class Qso(callsign: CallSign, bandMode: BandMode, exchange: Exchange, stamp: Instant = Instant.now()) {
  def isDup(that: Qso): Boolean = {
    this.callsign == that.callsign &&
      this.bandMode == that.bandMode
  }

}

/**
  *
  * @param nodeSn      sequential number from this network node. (probably not useful)
  * @param nodeAddress ip address of the network node.
  * @param uuid        unique id in time and space. Two QsoRecords with the same uuid can be considered equal.
  */
case class FdLogId(nodeSn: Int,
                   nodeAddress: String,
                   uuid: UUID = UUID.randomUUID) {
  override def equals(obj: Any): Boolean = uuid == this.uuid

}

object Contact {

  import org.wa9nnn.fdlog.model.ModeJson._
  //  implicit val bandFormat: Format[Band] = Json.format[Band]
  import Band._

  //  implicit val modeFormat: Format[Mode] = Json.format[Mode]
  implicit val transmitterFormat: Format[OurStation] = Json.format[OurStation]
  implicit val bandModeFormat: Format[BandMode] = Json.format[BandMode]
  implicit val qsoFormat: Format[Qso] = Json.format[Qso]
  implicit val fdLogIdFormat: Format[FdLogId] = Json.format[FdLogId]
  implicit val contactFormat: Format[QsoRecord] = Json.format[QsoRecord]
  type CallSign = String
}





