
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
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.javafx.cluster.{PropertyCell, PropertyCellName, SimplePropertyCell}
import org.wa9nnn.util.TimeHelpers.utcZoneId

import java.time.{Instant, ZonedDateTime}
import scala.collection.concurrent.TrieMap

/**
 * Id of a collection of QSOs in a calendar hour.
 * Its just a LocalDateTime with only any hour.
 *
 */
case class FdHour private(day: Int, hour: Int) extends Ordered[FdHour] with PropertyCellName {
  def toCell: Cell = Cell(display)

  val display: String = f"$day:$hour%02d"
  override val toolTip = s"utc date: $day hour: $hour"
  override val toString: String = {
    s"$day:$hour"
  }

  def propertyCell: PropertyCell[_] = {
    SimplePropertyCell(this, Cell(display)
      .withToolTip(toolTip)
      .withCssClass("clusterRowHeader"))
  }

  /**
   * Used for testing
   */
  def plus(addedHours: Int): FdHour = {
    val maybeHours = hour + addedHours
    if (maybeHours > 23) {
      FdHour(day + 1, maybeHours - 24)
    }
    else {
      copy(hour = maybeHours)
    }
  }

  override def compare(that: FdHour): Int = {
    val ret = this.day compareTo that.day
    if (ret == 0) {
      this.hour compareTo that.hour
    } else
      ret
  }

  override def name: String = display

}

object FdHour extends LazyLogging {
  /**
   * Used to match any FdHour in [[FdHour.equals()]]
   */
  val allHours: FdHour = FdHour(0, 0)
  lazy val knownHours: TrieMap[FdHour, FdHour] = new TrieMap[FdHour, FdHour]()

  def apply(day: Int, hour: Int): FdHour = {
    val candidate = new FdHour(day, hour)
    done(candidate)
  }

  def apply(instant: Instant = Instant.now()): FdHour = {
    val dt: ZonedDateTime = ZonedDateTime.ofInstant(instant, utcZoneId)
    val day: Int = dt.getDayOfMonth
    val hour: Int = dt.getHour
    val candidate = new FdHour(day, hour)
    done(candidate)
  }

  private def done(candidate: FdHour): FdHour = {
    knownHours.getOrElseUpdate(candidate, candidate)
  }
}
