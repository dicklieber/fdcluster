
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.BandMode.{Band, Mode}
import scalafx.beans.property.ObjectProperty

import java.util.prefs.Preferences
import javax.inject.Inject
import scala.util.matching.Regex

class BandModeStore @Inject()(preferences: Preferences)  extends ObjectProperty[BandMode]{

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

