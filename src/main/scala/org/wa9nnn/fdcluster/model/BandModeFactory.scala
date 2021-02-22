
package org.wa9nnn.fdcluster.model

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.AvailableBand.availaBandRegx
import org.wa9nnn.fdcluster.model.BandModeOperator._

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
}
