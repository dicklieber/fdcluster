
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

import java.io.PrintWriter

/**
 * Things the the [[AdifParser]] send to callback.
 */
sealed trait AdifResult {
  def toLine:String

}

object AdifResult {
  val eoh: AdifSeperator = AdifSeperator("EOH")
  val eor: AdifSeperator = AdifSeperator("EOR")
}

case class AdifEntry(tag: String, value: String) extends AdifResult with Ordered[AdifEntry] {
  assert(tag == tag.toUpperCase, s"tag must be all caps! got:$tag")

  def toLine: String = s"<$tag:${value.length}>$value\r\n"

  override def compare(that: AdifEntry): Int = {

    this.tag compareTo(that.tag)
  }
}

case class AdifSeperator(name: String) extends AdifResult {
  override def equals(obj: Any): Boolean = {
    obj match {
      case AdifSeperator(n) =>
        name equalsIgnoreCase (n)
      case _ => false
    }

  }

  override val toLine: String = s"<$name>"
}

