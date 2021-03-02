
package org.wa9nnn.fdcluster.adif

import org.wa9nnn.fdcluster._
import org.wa9nnn.fdcluster.model.{BandModeOperator, Exchange}

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.language.implicitConversions

object AdifQsoAdapter {
  private val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

  /**
   * //todo error & various formats handling
   * @param adif Qso
   * @return model Qso
   * @throws MissingRequiredTag if required tag not found
   */
  def apply(adif: Qso): model.Qso = {
    val map = adif.toMap
    /**
     * Allows a 'm' string, e.g. m"BAND" to lookup the key in the map and throw appropriate exception for missing tag.
     */
    implicit class mHelper(val sc: StringContext)  {
      def m(args: Any*): String = {
        val tagName = sc.parts.head
        try {
          map(tagName)
        } catch {
          case _:NoSuchElementException =>
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

    val stamp: LocalDateTime = {
      LocalDateTime.of(
        LocalDate.parse(m"QSO_DATE", BASIC_ISO_DATE),
        LocalTime.parse(m"TIME_ON", timeFormat)
      )
    }
    model.Qso(callsign = m"CALL",
      bandMode = bandMode,
      exchange = exchange,
      stamp = stamp
    )
  }
}
class MissingRequiredTag(tagName:String) extends Exception(s"Missing tag: $tagName")