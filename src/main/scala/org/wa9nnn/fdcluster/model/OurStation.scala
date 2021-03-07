
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{StructuredLogging, Persistence}
import scalafx.beans.property.ObjectProperty

import javax.inject.Inject

case class OurStation(ourCallsign: CallSign = "", exchange: Exchange = new Exchange(), rig: String = "", antenna: String = "") {
  def transmitters: Int = exchange.transmitters

}

class OurStationStore @Inject()(preferences: Persistence) extends ObjectProperty[OurStation] with StructuredLogging {
  value = preferences.loadFromFile[OurStation].getOrElse(OurStation())

  onChange { (_, _, newValue) =>
    preferences.saveToFile(newValue)
  }
}


