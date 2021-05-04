
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

package org.wa9nnn.fdcluster.model

import scala.util.matching.Regex


case class AvailableBand(band: String, freqStart: Int = 0, freqEnd: Int = 0) extends Ordered[AvailableBand] {
  def containsHz(frequency: Int): Boolean = frequency >= freqStart && frequency <= freqEnd

  override def compare(that: AvailableBand): Int = this.freqStart.compareTo(that.freqStart)
}

object AvailableBand {
  val availaBandRegx: Regex = """([a-zA-Z0-9.]{1,5}):\s+([0-9.]+)\s+to\s+([0-9.]+)""".r

  def apply(confString:String): AvailableBand = {
    val availaBandRegx(contestBand, startMhz, endMhz) = confString
    val startHz:Int = (startMhz.toDouble * 1000000.0).toInt
    val endHz:Int = (endMhz.toDouble * 1000000.0).toInt
    AvailableBand(contestBand, startHz, endHz)
  }
}

