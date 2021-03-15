
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

import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}
import org.wa9nnn.fdcluster.model.{BandModeFactory, BandModeOperator}

import java.security.SecureRandom
import javax.inject.Inject

class RandomBandMode @Inject()(bandModeFactory:BandModeFactory = new BandModeFactory()){
  val random = new SecureRandom()
  val modes: List[Mode] = bandModeFactory.modes.map(_.mode)
  val bands: List[Band] = bandModeFactory.avalableBands.map(_.band)

  def next: BandModeOperator = {
    BandModeOperator(  bands(random.nextInt(bands.length)),
      modes(random.nextInt(modes.length)))
  }
}
