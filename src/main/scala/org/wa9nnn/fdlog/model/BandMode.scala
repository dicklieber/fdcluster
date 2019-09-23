
package org.wa9nnn.fdlog.model

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.wa9nnn.fdlog.model.BandMode._

import scala.collection.JavaConverters._
import scala.util.matching.Regex

/**
 * safer to construct via {{org.wa9nnn.fdlog.model.BandModeFactory#apply(java.lang.String, java.lang.String)}}
 */
case class BandMode(band: Band = "20m", mode: Mode = "phone")


object BandMode {

  type Band = String
  type Mode = String

  val regex: Regex = """(.*);(.*)""".r
}

class BandModeFactory @Inject()(config: Config = ConfigFactory.load()) extends LazyLogging{
  private val bandModeConfig: Config = config.getConfig("fdlog.bandMode")

  val bands: List[String] = bandModeConfig.getStringList("bands").asScala.toList

  val bandSet: Set[String] = bands.toSet

  val modes: List[String] = bandModeConfig.getStringList("modes").asScala.toList.sorted
  val modeSet: Set[String] = bands.toSet

  def ckeckBand(band: Band): Band = {
    if (bandSet.contains(band))
      band
    else
      bands.head
  }

  def checkMode(band: Mode): Mode = {
    if (modeSet.contains(band))
      band
    else
      bands.head
  }

  def apply(band: Band, mode: Mode): BandMode = {
    new BandMode(ckeckBand(band), checkMode(mode))
  }
}