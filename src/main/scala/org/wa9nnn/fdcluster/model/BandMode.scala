package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.Station.{Band, Mode}
import play.api.libs.json._

/**
 * Allows storng band and mode in a compact why in a [[Qso]]
 */
case class BandMode(bandName: Band = "20m", modeName: Mode = "PH") {
  override def toString: String = s"$bandName $modeName"
}

object BandMode {
  private val Parse = """\s*([\d.]+[a-z]+)\s+([A-Z]{2})\s*""".r

  def apply(s:String):BandMode = {
    val Parse(bandName, modeName) = s
    new BandMode(bandName, modeName)
  }

  /**
   * to make JSON a bit more compact
   */
  implicit val bmFormat: Format[BandMode] = new Format[BandMode] {
    override def reads(json: JsValue): JsResult[BandMode] = {
      val ss = json.as[String]
      try {
        ss match {
          case Parse(bandName, modeName) ⇒
            JsSuccess(BandMode(bandName, modeName))
          case _ ⇒
            JsError(s"BandMode: $ss could not be parsed!")
        }
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(bandMode: BandMode): JsValue = {
      JsString(bandMode.toString)
    }
  }

}