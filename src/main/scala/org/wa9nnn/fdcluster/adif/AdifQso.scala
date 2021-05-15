
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

package org.wa9nnn.fdcluster.adif

import java.time.Duration

case class AdifFile(header: Seq[AdifEntry], records: Seq[AdifQso])

case class AdifQso(entries: Set[AdifEntry]) {
  def contains(that: AdifQso): Boolean = {
    val intersection = that.entries.intersect(this.entries)
    intersection == that.entries
  }

  def toMap: Map[String, String] = entries.map(e =>
    e.tag.toUpperCase -> e.value).toMap
}
