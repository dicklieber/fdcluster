
package org.wa9nnn.fdcluster.store.network

import java.time.{LocalDate, LocalDateTime}

import com.typesafe.scalalogging.LazyLogging

import scala.collection.concurrent.TrieMap


/**
 * Id of a collection of QSOs in a calendar hour.
 * Its just a LocalDateTime with only any hour.
 *
 */
case class FdHour(localDate: LocalDate, hour: Int) extends Ordered[FdHour] {
  assert(hour >= 0 && hour <= 23, "hour must be between 0 and 23!")

  lazy val day: Int = localDate.getDayOfMonth

  override def equals(obj: Any): Boolean = {
    obj match {
      case fd: FdHour ⇒
        if (fd.localDate == FdHour.allHours.localDate && fd.hour == FdHour.allHours.hour) {
          true
        } else {
          localDate == fd.localDate && hour == fd.hour
        }
      case _ ⇒
        false
    }
  }

  def plus(i: Int): FdHour = {
    if (hour == 23)
      FdHour(localDate.plusDays(1), 0)
    else
      FdHour(localDate, hour + 1)
  }

  override def compare(that: FdHour): Int = {
    var ret = this.localDate compareTo that.localDate
    if (ret == 0) {
      ret = this.hour compareTo that.hour
    }
    ret
  }

  override def toString: String = {
    f"$day:$hour%02d"
  }
}

object FdHourStuff {
  val knownFDHours = new TrieMap[FdHour, FdHour]

}

object FdHour extends LazyLogging {
  /**
   * Used to match any FdHour in [[FdHour.equals()]]
   */
  val allHours = FdHour(LocalDateTime.MIN)

  def apply(localDateTime: LocalDateTime): FdHour = {
    val need = new FdHour(localDateTime.toLocalDate, localDateTime.getHour)
    val ret = FdHourStuff.knownFDHours.getOrElseUpdate(need, need)
    if (ret eq need) {
      logger.trace("New fdhour")
    } else {
      logger.trace("reusing fdhour")
    }

    ret
  }
}
