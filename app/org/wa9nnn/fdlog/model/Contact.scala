package org.wa9nnn.fdlog.model

import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json

/**
  *
  * @param contest e.g. WFD2017
  * @param callSign of the station doing the logging.
  * @param operator call sign of the operator.
  * @param workedStation call sign of the worked station.
  * @param band e.g. 40M, 222
  * @param nodeSn serial number from this nodeAddress. Used to find lost Contacts for a node.
  * @param nodeAddress IP addrsss of the node that created this Contact.
  * @param exchange contest-specific info. For field day would be category and section. e.g. "4A,IL"
  * @param uuid unique identifier of the Contact. Allows Contacts to be played back idempotently.
  * @param stamp when Contact was made.
  */
case class Contact(contest:String,
                   callSign:String,
                   operator: String,
                   workedStation: String,
                   band: String,
                   nodeSn: Int,
                   nodeAddress: String,
                   exchange: String,
                   uuid: UUID = UUID.randomUUID,
                   stamp: Instant = Instant.now())


object Contact{
  implicit val residentFormat = Json.format[Contact]
}





