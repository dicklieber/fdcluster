
package org.wa9nnn.fdcluster.adif

import org.wa9nnn.fdcluster.model.{BandModeOperator, Exchange, Qso}
import org.wa9nnn.fdcluster.{model, _}
import org.wa9nnn.util.TimeHelpers.utcZoneId

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import java.time.{Instant, LocalDate, LocalTime, ZonedDateTime}
import scala.language.implicitConversions

object AdifQsoAdapter {
  private val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

  /**
   * //todo error & various formats handling
   *
   * @param adif Qso
   * @return model Qso
   * @throws MissingRequiredTag if required tag not found
   */
  def apply(adif: AdifQso): model.Qso = {
    val map = adif.toMap
    /**
     * Allows a 'm' string, e.g. m"BAND" to lookup the key in the map and throw appropriate exception for missing tag.
     */
    implicit class mHelper(val sc: StringContext) {
      def m(args: Any*): String = {
        val tagName = sc.parts.head
        try {
          map(tagName)
        } catch {
          case _: NoSuchElementException =>
            throw new MissingRequiredTag(tagName)
          case x =>
            throw x
        }
      }
    }

    val bandMode = BandModeOperator(
      bandName = m"BAND",
      modeName = m"MODE"
    )
    val exchange = Exchange(m"CLASS", m"ARRL_SECT")

    val stamp: Instant = {
      ZonedDateTime.of(
        LocalDate.parse(m"QSO_DATE", BASIC_ISO_DATE),
        LocalTime.parse(m"TIME_ON", timeFormat),
        utcZoneId).toInstant
    }
    model.Qso(callsign = m"CALL",
      bandMode = bandMode,
      exchange = exchange,
      stamp = stamp
    )
  }

  def apply(model: Qso): adif.AdifQso = {
    implicit def e(t2: (String, String)): AdifEntry = {
      AdifEntry(t2._1, t2._2)
    }

    val zdt = ZonedDateTime.ofInstant(model.stamp, utcZoneId)
    val entries = Set.newBuilder[AdifEntry]
    entries += "QSO_DATE" -> zdt.toLocalDate.format(BASIC_ISO_DATE)
    entries += "TIME_ON" -> zdt.toLocalTime.format(timeFormat)
    entries += "CALL" -> model.callsign
    entries += "BAND" -> model.bandMode.bandName
    entries += "MODE" -> model.bandMode.modeName
    entries += "CLASS" -> model.exchange.entryClass
    entries += "ARRL_SECT" -> model.exchange.section

    adif.AdifQso(entries.result())
  }
}

class MissingRequiredTag(tagName: String) extends Exception(s"Missing tag: $tagName")