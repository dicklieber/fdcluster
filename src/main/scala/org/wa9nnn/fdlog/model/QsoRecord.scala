package org.wa9nnn.fdlog.model

import java.net.InetAddress
import java.time.LocalDateTime
import java.util.UUID

import akka.util.ByteString
import org.wa9nnn.fdlog.model.MessageFormats.{CallSign, _}
import org.wa9nnn.fdlog.model.sync.FdHour
import play.api.libs.json.Json

/**
 * This is what's in the store and journal.log.
 *
 * @param contest       ARRL winter and year
 * @param ourStation    within our site.
 * @param qso           who we worked.
 * @param fdLogId       housekeeping info for replication.
 */
case class QsoRecord(contest: Contest,
                     ourStation: OurStation,
                     qso: Qso,
                     fdLogId: FdLogId) extends Ordered[QsoRecord] {
  def callsign: CallSign = qso.callsign

  def uuid: String = fdLogId.uuid


  override def hashCode: Int = fdLogId.uuid.hashCode()

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
}

/**
 * One contact with another station.
 *
 */
case class Qso(callsign: CallSign, bandMode: BandMode, exchange: Exchange, stamp: LocalDateTime = LocalDateTime.now()) {
  def isDup(that: Qso): Boolean = {
    this.callsign == that.callsign &&
      this.bandMode == that.bandMode
  }
}

/**
 * Info used internally by FDLog.
 *
 * @param nodeSn      sequential number from this network node. (probably not useful)
 * @param nodeAddress ip address of the network node.
 * @param uuid        unique id in time and space. Two QsoRecords with the same uuid can be considered equal.
 */
case class FdLogId(nodeSn: Int,
                   nodeAddress: NodeAddress,
                   uuid: String = UUID.randomUUID.toString) {
  override def equals(obj: Any): Boolean = uuid == this.uuid

}

case class NodeAddress(instance: Int = 0, nodeAddress: String = InetAddress.getLocalHost.getHostAddress) {
   def display: String = {
    s"$nodeAddress:$instance"
  }
}

/**
 * This is what gets multicasted to cluster.
 *
 * @param qsoRecord the new QSO
 * @param size      number of QSOs in the database on this node. (Includes the new QSO)
 */
case class DistributedQsoRecord(qsoRecord: QsoRecord, size: Int) {
  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }

}

object DistributedQsoRecord {
  def apply(byteString: ByteString): DistributedQsoRecord = {
    Json.parse(byteString.decodeString("UTF-8")).as[DistributedQsoRecord]
  }
}






