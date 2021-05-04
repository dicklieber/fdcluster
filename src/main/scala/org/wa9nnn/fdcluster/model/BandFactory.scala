
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

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.CurrentStation._

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/**
 * Provides available bands and modes.
 *
 * @param config access to application.conf.
 */
@Singleton
class BandFactory @Inject()(config: Config = ConfigFactory.load()) extends LazyLogging {

  /**
   * All the bands that can be used.
   * Currently all bands for WFD and ARRL Field day.
   *
   */
  val availableBands: List[AvailableBand] = config.getStringList("contest.bands").asScala.toList.map { s =>
    AvailableBand(s)
  }.sorted

  /**
   * Find a band for a frequency.
   *
   * @param frequencyHz in Hz
   * @return
   */
  def band(frequencyHz: Int): Option[Band] = {
    val maybeBand: Option[AvailableBand] = availableBands.find(ab => ab.containsHz(frequencyHz))
    maybeBand.map(_.band)
  }

}
