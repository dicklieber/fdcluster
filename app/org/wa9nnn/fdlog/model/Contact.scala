package org.wa9nnn.fdlog.model

import java.time.Instant
import java.util.UUID

import org.wa9nnn.fdlog.model.Contact.{OperatorCallsign, WorkedCallsign}
import play.api.libs.json.{Format, Json}

/**
  *
  * @param contest               e.g. WFD:2017
  * @param operatorCallsign      of the station doing the logging.
  * @param workedStation         call sign of the worked station.
  * @param band                  e.g. 40M, 222
  * @param mode                  phone, cw or gigital
  * @param nodeSn                serial number from this nodeAddress. Used to find lost Contacts for a node.
  * @param nodeAddress           IP addrsss of the node that created this Contact.
  * @param exchange              contest-specific info. For field day would be category and section. e.g. "4A,IL"
  * @param uuid                  unique identifier of the Contact. Allows Contacts to be played back idempotently.
  * @param stamp                 when Contact was made.
  */
case class Contact(contest: Contest,
                   operatorCallsign: OperatorCallsign,
                   workedStation: WorkedCallsign,
                   band: String,
                   mode: Mode,
                   nodeSn: Int,
                   nodeAddress: String,
                   exchange: Exchange,
                   uuid: UUID = UUID.randomUUID,
                   stamp: Instant = Instant.now()) extends Ordered[Contact] {
  override def compare(that: Contact): Int = this.workedStation compareToIgnoreCase that.workedStation

  override def equals(o: Any): Boolean = o match {
    case c: Contact ⇒
      this.uuid == uuid
    case _ ⇒
      false
  }

  override def hashCode: Int = uuid.hashCode()

  def dup(contact: Contact): Boolean = {
    band == contact.band &&
      mode == contact.mode &&
      workedStation == contact.workedStation
  }

}

case class PotentialContact(operatorCallsign: OperatorCallsign,
                            workedStation: WorkedCallsign,
                            exchange: Exchange,
                            band: String,
                            mode: Mode) {
  def toContact(implicit nodeInfo: NodeInfo): Contact = {
    Contact(contest = nodeInfo.contest,
      operatorCallsign = operatorCallsign,
      workedStation = workedStation,
      band = band,
      mode = mode,
      nodeSn = nodeInfo.nextSn,
      nodeAddress = nodeInfo.node,
      exchange = exchange
    )
  }
}

object Contact {

  import ModeJson.modeFormat

  implicit val contactFormat: Format[Contact] = Json.format[Contact]
  type WorkedCallsign = CallSign
  type OperatorCallsign = CallSign
  type CallSign = String
}





