
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.javafx.entry.EntryCategory
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.JsonLogging
import play.api.libs.json.Json
import scalafx.beans.property.ObjectProperty

import java.time.Instant
import java.util.prefs.Preferences
import javax.inject.Inject

case class OurStation(ourCallsign: CallSign = "-", exchange: Exchange = Exchange("-","-"), rig: String = "", antenna: String = "", stamp: Instant = Instant.now()) {
  def transmitters: Int = exchange.transmitters.toInt
  def category:Option[EntryCategory] = exchange.maybeEntryCategory

  def encode: String = Json.toJson(this).toString()
}

object OurStation {
  def decode(json: String): OurStation = {
    json match {
      case "" =>
        OurStation()
      case json =>
        Json.parse(json).as[OurStation]
    }
  }
}

class OurStationStore @Inject()(preferences: Preferences) extends ObjectProperty[OurStation] with JsonLogging {
  value = {
    try {
      val json = preferences.get("ourStation", "")
      OurStation.decode(json)
    } catch {
      case _:Exception =>
        OurStation()
    }
  }

  onChange{(_,_,newValue) =>
    preferences.put("ourStation", newValue.encode)
  }
}


