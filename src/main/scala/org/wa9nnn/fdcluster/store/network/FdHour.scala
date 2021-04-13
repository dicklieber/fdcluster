
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.store.network

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.cluster.LabelSource
import org.wa9nnn.util.TimeHelpers.{msHour, utcZoneId}
import scalafx.scene.control.Labeled

import java.time.{Instant, ZonedDateTime}
import scala.collection.concurrent.TrieMap

/**
 * Id of a collection of QSOs in a calendar hour.
 * Its just a LocalDateTime with only any hour.
 *
 */
case class FdHour(epochHours: Long) extends Ordered[FdHour] with LabelSource {
  val instant = Instant.ofEpochMilli(epochHours * msHour)
  val dt: ZonedDateTime = ZonedDateTime.ofInstant(instant, utcZoneId)
  val day: Int = dt.getDayOfMonth
  val hour: Int = dt.getHour

  val display: String = f"$day:$hour%02d"
  override val toolTip = s"utc date: $day hour: $hour"
  override val toString: String = {
    s"$day:$hour"
  }

  /**
   * Used for testing
   */
  def plus(addedHours: Int): FdHour = {
    FdHour(epochHours + addedHours)
  }

  override def compare(that: FdHour): Int = {
    this.epochHours compareTo that.epochHours
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: FdHour =>
        this.epochHours == that.epochHours
      case _ =>
        false
    }
  }

  override def setLabel(labeled: Labeled): Unit = {
    labeled.tooltip = s"utc date: $day hour: $hour ($dt)"

    labeled.text = toString
  }
}

object FdHour extends LazyLogging {
  /**
   * Used to match any FdHour in [[FdHour.equals()]]
   */
  val allHours: FdHour = FdHour(Long.MinValue)
  val knownHours = new TrieMap[Long, FdHour]()

  def apply(instant: Instant): FdHour = {
    val epochHours = instant.toEpochMilli / msHour
    knownHours.getOrElseUpdate(epochHours, new FdHour(epochHours))
  }
}
