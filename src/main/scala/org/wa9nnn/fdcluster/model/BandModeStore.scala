
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.BandMode.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.beans.property.ObjectProperty

import javax.inject.Inject
import scala.util.matching.Regex

class BandModeStore @Inject()(persistence: Persistence) extends ObjectProperty[BandMode] {
  value = persistence.loadFromFile[BandMode]().getOrElse(BandMode())
  onChange { (_, _, newValue) => {
    persistence.saveToFile(newValue)
  }
  }
}

/**
 * safer to construct via {{org.wa9nnn.fdlog.model.BandModeFactory#apply(java.lang.String, java.lang.String)}}
 */
case class BandMode(bandName: Band = "20m", modeName: Mode = "phone") {
  override def toString: String = s"$bandName"
}

object BandMode {

  type Band = String
  type Mode = String

  val regex: Regex = """(.*);(.*)""".r
}

