
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.beans.property.ObjectProperty

import javax.inject.Inject

class BandModeOperatorStore @Inject()(persistence: Persistence) extends ObjectProperty[BandModeOperator] {
  value = persistence.loadFromFile[BandModeOperator]().getOrElse(BandModeOperator())
  onChange { (_, _, newValue) => {
    persistence.saveToFile(newValue)
  }
  }
}

/**
 * safer to construct via {{org.wa9nnn.fdlog.model.BandModeFactory#apply(java.lang.String, java.lang.String)}}
 */
case class BandModeOperator(bandName: Band = "20m", modeName: Mode = "PH", operator:CallSign = "") {
  override def toString: String = s"$bandName"
}

object BandModeOperator {
  type Band = String
  type Mode = String
}
