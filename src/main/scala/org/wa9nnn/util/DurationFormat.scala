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

package org.wa9nnn.util

import java.time.{Duration, Instant}
import scala.language.implicitConversions

/**
 * Format a [[java.time.Duration]] to nice human readable, scaled string.
 */
object DurationFormat {

  val ONE_SECOND = 1000L
  val ONE_MINUTE = 60000L
  val ONE_HOUR = 3600000L
  val ONE_DAY = 86400000L


  /**
   *
   * @param duration how long
   * @return string e.g. "59 min 2 sec" or "999 ms"
   */
  implicit def apply(duration: Duration): String = {
    duration.toMillis match {
      case ms if ms < ONE_SECOND => f"${duration.toMillis} ms"
      case ms if ms == ONE_SECOND => "1 sec"
      case ms if ms < ONE_MINUTE =>
        val remaininMs = ms % ONE_SECOND
        f"${ms / ONE_SECOND}%d sec $remaininMs%d ms"
      case ms if ms == ONE_MINUTE => "1 min"
      case ms if ms < ONE_HOUR =>
        val minutes = duration.toMinutes
        val remaininSecs = (ms - minutes * ONE_MINUTE) / ONE_SECOND
        f"$minutes%d min $remaininSecs%d sec"
      case ms if ms < ONE_DAY =>
        val hours = ms / ONE_HOUR
        val minutes = (ms - hours * ONE_HOUR) / ONE_MINUTE
        f"$hours%d hours $minutes%d min"
      case ms =>
        val days = ms / ONE_DAY
        val hours = (ms - days * ONE_DAY) / ONE_HOUR
        val mins = (ms - (hours * ONE_HOUR + days * ONE_DAY)) / ONE_MINUTE
        if (mins == 0) {
          f"$days%d day $hours%d hour"
        } else {
          f"$days%d day $hours%d hour $mins%d min"
        }
    }
  }

  def apply(instant: Instant): String = {
    apply(Duration.between(instant, Instant.now()))
  }

}
