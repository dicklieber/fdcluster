package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.BandMode.bandToFreq
import org.wa9nnn.fdcluster.model.Station.{Band, Mode}
import play.api.libs.json._

/**
 * Allows storng band and mode in a compact why in a [[Qso]]
 */
case class BandMode(bandName: Band = "20m", modeName: Mode = "PH") {
  def cabMode: Band = {
    modeName match {
      case "USB" => "PH"
      case "LSB" => "PH"
      case "SSB" => "PH"
      case "AM" => "PH"
      case "CW" => "CW"
      case _ => "DI"
    }
  }

  override def toString: String = s"$bandName $modeName"

  lazy val cabFreq: String = {
    bandToFreq(bandName)
  }

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

  /**
   * Use when we don't have an explicit frequency
   */
  val bandFreqMap: Map[Band, Band] = {
    Seq(
      "160M" -> "1810",
      "80M" -> "3530",
      "40M" -> "7030",
      "20M" -> "14035",
      "15M" -> "21030",
      "10M" -> "28030",
      "6M" -> "28030",
      "2M" -> "144000",
      "1.25M" -> "224000",
      "70cm" -> "442000",
    ).toMap
  }

  def bandToFreq(band: String): String = {
    bandFreqMap.getOrElse(band.toUpperCase(), "")
  }

}