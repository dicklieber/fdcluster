
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}

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

