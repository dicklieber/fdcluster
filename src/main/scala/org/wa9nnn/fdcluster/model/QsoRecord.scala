package org.wa9nnn.fdcluster.model

import akka.util.ByteString
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json

import java.time.Instant
import java.util.UUID

/**
 * * One contact with another station.
 * * Things that are relevant for the contest.
 *
 * @param callsign of the worked station.
 * @param bandMode that was used.
 * @param exchange from the worked station.
 * @param stamp    when QSO occurred.
 */
case class Qso(callsign: CallSign, bandMode: BandModeOperator, exchange: Exchange, stamp: Instant = Instant.now()) {
  def isDup(that: Qso): Boolean = {
    this.callsign == that.callsign &&
      this.bandMode == that.bandMode
  }
}


/**
 * This is what's in the store and journal.log.
 *
 * @param contest       ARRL winter and year
 * @param ourStation    within our site.
 * @param qso           who we worked.
 * @param fdLogId       housekeeping info for replication.
 */
case class QsoRecord(qso: Qso, contest: Contest, ourStation: OurStation, fdLogId: FdLogId) extends Ordered[QsoRecord] {
  def callsign: CallSign = qso.callsign

  def uuid: String = fdLogId.uuid

  lazy val display: String = s"$callsign on ${qso.bandMode} at ${fdLogId.nodeAddress.display} in $fdHour"

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

case class QsosFromNode(nodeAddress: NodeAddress, qsos: List[QsoRecord]) {
  def size: Int = qsos.size

}

/**
 * Info used internally by FDLog.
 *
 * @param nodeAddress ip address of the network node.
 * @param uuid        unique id in time and space. Two QsoRecords with the same uuid can be considered equal.
 */
case class FdLogId(nodeAddress: NodeAddress, uuid: String = UUID.randomUUID.toString) {
  override def equals(obj: Any): Boolean = uuid == this.uuid

}


/**
 * This is what gets multi-casted to cluster.
 *
 * @param qsoRecord   the new QSO
 * @param nodeAddress where this came from.
 * @param size        number of QSOs in the database on this node. (Includes the new QSO)
 */
case class DistributedQsoRecord(qsoRecord: QsoRecord, nodeAddress: NodeAddress, size: Int) extends Codec {
  def toByteString: ByteString = {
    import MessageFormats._
    ByteString(Json.toBytes(Json.toJson(this)))
  }
}

object DistributedQsoRecord {
  val qsoVersion = "1:"
  def apply(byteString: ByteString): DistributedQsoRecord = {
    val sJson = byteString.decodeString("UTF-8")
    val jsValue = Json.parse(sJson)
    try {
      jsValue.as[DistributedQsoRecord]
    } catch {
      case e:Exception =>
        e.printStackTrace()
        throw e
    }
  }
}

/**
 * Something that can be rendered as a JSON string.
 */
trait Codec {
  def toByteString: ByteString
}


