
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
import org.wa9nnn.fdcluster.model.AvailableBand.availaBandRegx
import org.wa9nnn.fdcluster.model.CurrentStation._
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign

import javax.inject.Inject
import scala.jdk.CollectionConverters._

class BandModeFactory @Inject()(config: Config = ConfigFactory.load()) extends LazyLogging {
  def modeForRig(rig: String): Option[String] = {
    modes.find(_.rigModes.contains(rig)).map(_.mode)
  }

  /**
   * All the bands that can be used.
   * Currently all band for WFD and ARRL Field day.
   *
   */
  val avalableBands: List[AvailableBand] = config.getStringList("fdcluster.bandMode.bands").asScala.toList.map {

    s =>
      val availaBandRegx(band, from, to) = s
      AvailableBand(band, from.toInt, to.toInt)
  }.sorted

  /**
   * Find a band for a frequency.
   *
   * @param frequency in Kh
   * @return
   */
  def band(frequency: Int): Option[Band] = {
    val maybeBand: Option[AvailableBand] = avalableBands.find(ab => ab.containsFfreq(frequency))
    maybeBand.map(_.band)
  }

  val modes: List[AvailableMode] = {

    val config1 = config.getConfig("fdcluster.bandMode.modes")
    config1
      .entrySet
      .asScala
      .toList
      .sortBy(_.getKey)
      .map { (entry) =>
        AvailableMode(
          entry.getKey,
          entry.getValue
            .toString
            .split("""\s+""").toList
        )
      }
  }
  val modeMapping: Map[String, String] = {
    (for {
      am <- modes
      rigMode <- am.rigModes
    } yield {
      rigMode -> am.mode
    }).toMap
  }
  def bandModeOperator(bandName: Band = "20m", modeName: Mode = "PH", operator: CallSign = ""):CurrentStation = {
    //todo handle band validation.
    new CurrentStation(bandName, modeMapping(modeName), operator)
  }
}
