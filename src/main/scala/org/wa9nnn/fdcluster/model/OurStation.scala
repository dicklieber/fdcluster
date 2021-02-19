
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.javafx.entry.EntryCategory
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{JsonLogging, Persistence}
import scalafx.beans.property.ObjectProperty

import java.time.Instant
import javax.inject.Inject

case class OurStation(ourCallsign: CallSign = "", exchange: Exchange = new Exchange(), rig: String = "", antenna: String = "", stamp: Instant = Instant.now()) {
  def transmitters: Int = exchange.transmitters

}

class OurStationStore @Inject()(preferences: Persistence) extends ObjectProperty[OurStation] with JsonLogging {
  value = preferences.loadFromFile[OurStation].getOrElse(OurStation())

  onChange { (_, _, newValue) =>
    preferences.saveToFile(newValue)
  }
}


