
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

import org.wa9nnn.fdcluster.model.CurrentStation.{Band, Mode}

import scala.util.matching.Regex


case class AvailableBand(band: String, freqStart: Int = 0, freqEnd: Int = 0) extends Ordered[AvailableBand] {
  def containsFfreq(frequency: Int): Boolean = frequency >= freqStart && frequency <= freqEnd

  override def compare(that: AvailableBand): Int = this.freqStart.compareTo(that.freqStart)
}

object AvailableBand {
  val availaBandRegx: Regex = """(\d+(?:\.\d+)?c?m)\s*:\s(\d+)\s*to\s*(\d+)""".r

  def apply(): AvailableBand = {
    throw new NotImplementedError() //todo
  }
}

/**
 *
 * @param mode context mode
 * @param rigModes modes that map to [[mode]]
 */
case class AvailableMode(mode:Mode, rigModes:List[Mode])

