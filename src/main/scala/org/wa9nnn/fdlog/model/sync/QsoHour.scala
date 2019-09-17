
package org.wa9nnn.fdlog.model.sync

import java.security.MessageDigest

import org.wa9nnn.fdlog.model.MessageFormats.{Digest, Uuid}
import org.wa9nnn.fdlog.model.QsoRecord
import org.wa9nnn.fdlog.store.network.FdHour


/**
 *
 * @param startOfHour truncated to the hour.
 * @param qsos        QSOs in this hour.
 */
case class QsoHour(startOfHour: FdHour, qsos: List[QsoRecord]) {

  lazy val hourDigest: QsoHourDigest = {
    val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
    qsos.foreach(qr ⇒ messageDigest.update(qr.fdLogId.uuid.getBytes()))
    val bytes = messageDigest.digest()
    val encoder = java.util.Base64.getEncoder
    val bytes1 = encoder.encode(bytes)
    val sDigest = new String(bytes1)
    QsoHourDigest(startOfHour, sDigest, qsos.size)
  }

  lazy val qsoIds: QsoHourIds = {
    val ids = qsos.map(_.fdLogId.uuid)
    QsoHourIds(startOfHour, ids)
  }
}

object QsoHour {
  def apply(qsos: List[QsoRecord]): QsoHour = {
    assert(qsos.nonEmpty, "Must have some qsos in an hour.")
    val startOfHour = qsos.head.fdHour
    QsoHour(startOfHour, qsos)
  }
}

/**
 * Used to quickly compare one node's hour with another.
 *
 * @param startOfHour truncated to the hour.
 * @param digest      of all the QsoIDs in this hour.
 * @param size        number of Qsos in this hour.  //todo Do we actually need this? isn't the digest sufficient?
 */
case class QsoHourDigest(startOfHour: FdHour, digest: Digest, size: Int)

case class QsoHourIds(startOfHour: FdHour, qsiIds: List[Uuid])
