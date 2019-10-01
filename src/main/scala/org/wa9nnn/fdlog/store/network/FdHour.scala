
package org.wa9nnn.fdlog.store.network

import java.time.LocalDateTime


/**
 * Id of a collection of QSOs in a calendar hour.
 *
 * This works because a Field Day can't span a month.
 *
 * @param day  of month
 * @param hour of day
 */
case class FdHour(day: Int, hour: Int) extends Ordered[FdHour] {
  override def equals(obj: Any): Boolean = {
    obj match {
      case fd:FdHour ⇒
        if (fd.hour == FdHour.allHours.hour && fd.day == FdHour.allHours.day) {
          true
        } else {
          fd.hour == hour && fd.day == day
        }
      case _ ⇒
        false
    }
  }

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

  override def toString: String = {
    f"$day:$hour%02d"
  }
}

object FdHour {
  val allHours = FdHour(0, 0)

  def apply(localDateTime: LocalDateTime): FdHour = {
    FdHour(localDateTime.getDayOfMonth, localDateTime.getHour)
  }
}