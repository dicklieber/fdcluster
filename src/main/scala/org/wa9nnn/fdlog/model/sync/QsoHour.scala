
package org.wa9nnn.fdlog.model.sync

import java.security.MessageDigest
import java.time.LocalDateTime

import org.wa9nnn.fdlog.model.MessageFormats.Uuid
import org.wa9nnn.fdlog.model.QsoRecord


/**
 *
 * @param startOfHour truncated to the hour.
 * @param qsos        QSOs in this hour.
 */
case class QsoHour(startOfHour: FdHour, qsos: List[QsoRecord]) {
  lazy val hourDigest: QsoHourDigest = {
    val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
    qsos.foreach(qr ⇒ messageDigest.update(qr.fdLogId.uuid.getBytes()))
    val sDigest = java.util.Base64.getEncoder.encode(messageDigest.digest()).toString
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
case class QsoHourDigest(startOfHour: FdHour, digest: String, size: Int)

case class QsoHourIds(startOfHour: FdHour, qsiIds: List[Uuid])

/**
 * This works because a Field Day can't span a year.
 *
 * @param day  of month
 * @param hour of day
 */
case class FdHour(day: Int, hour: Int) extends Ordered[FdHour] {
  def plus(i: Int): FdHour = {
    if (hour == 23) {
      FdHour(day + 1, 0)
    } else {
      copy(hour = hour + 1)
    }
  }

  override def compare(that: FdHour): Int = {
    var ret = this.day compareTo that.day
    if (ret == 0) {
      ret = this.hour compareTo that.hour
    }
    ret
  }
}

object FdHour {
  def apply(localDateTime: LocalDateTime): FdHour = {
    FdHour(localDateTime.getDayOfMonth, localDateTime.getHour)
  }
}