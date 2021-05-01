
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

package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.model.CallSign
import org.wa9nnn.fdcluster.model.CallSign

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import scala.language.implicitConversions

class RandomCallsign {
  val p1 = new RandomChar("AKNW")
  val p2 = new RandomChar() // may be space
  val n = new RandomChar("01234567890")
  val s1 = new RandomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  val s2 = new RandomChar() // may be space
  val s3 = new RandomChar() // may be space

  def next: CallSign = {
    val cs = new String(Array(
      p1.nextChar,
      p2.nextChar,
      n.nextChar,
      s1.nextChar,
      s2.nextChar,
      s3.nextChar,
    ))
    cs.replace(" ", "") // remove spaces
  }

}







