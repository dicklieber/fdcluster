
package org.wa9nnn.fdcluster.model.sync

import java.security.MessageDigest

import org.wa9nnn.fdcluster.javafx.cluster.LabelSource
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{QsoRecord, sync}
import org.wa9nnn.fdcluster.store.network.FdHour
import scalafx.scene.control.Labeled

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
    sync.QsoHourDigest(startOfHour, sDigest, qsos.size)
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
case class QsoHourDigest(startOfHour: FdHour, digest: Digest, size: Int)extends LabelSource {
  override def setLabel(labeled: Labeled): Unit = {
    if (size == 0) {
      labeled.text = "--"
      labeled.tooltip = "No QSOs for this hour."
    } else {
      labeled.tooltip = "Qso Count: ${qsoHourDigest.size}\ndigest: ${qsoHourDigest.digest}\nDigest is based on all the QSO UUIDs in the hour."
      labeled.text = s"$size: ${digest.take(10)}..."
    }

  }
}

case class QsoHourIds(startOfHour: FdHour, qsiIds: List[Uuid])
