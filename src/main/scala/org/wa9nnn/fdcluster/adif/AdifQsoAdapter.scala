
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.adif

import org.wa9nnn.fdcluster.model.{BandMode, Exchange, Qso}
import org.wa9nnn.fdcluster.{model, _}
import org.wa9nnn.fdcluster.model.CallSign.s2cs
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
          case e: NoSuchElementException =>
            throw new MissingRequiredTag(tagName)
          case x:Throwable =>
            throw x
        }
      }
    }

    val bandMode = BandMode(
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
    model.Qso(callSign = m"CALL",
      bandMode = bandMode,
      exchange = exchange,
      stamp = stamp
    )
  }

  def apply(qso: Qso): adif.AdifQso = {
    implicit def e(t2: (String, String)): AdifEntry = {
      AdifEntry(t2._1, t2._2)
    }

    val zdt = ZonedDateTime.ofInstant(qso.stamp, utcZoneId)
    val entries = Set.newBuilder[AdifEntry]
    entries += "APP_FDC_UUID" -> qso.uuid.toString
    entries += "QSO_DATE" -> zdt.toLocalDate.format(BASIC_ISO_DATE)
    entries += "TIME_ON" -> zdt.toLocalTime.format(timeFormat)
    entries += "CALL" -> qso.callSign.callSign
    entries += "BAND" -> qso.bandMode.bandName
    entries += "MODE" -> qso.bandMode.modeName
    entries += "CLASS" -> qso.exchange.entryClass
    entries += "ARRL_SECT" -> qso.exchange.sectionCode

    adif.AdifQso(entries.result())
  }
}

class MissingRequiredTag(tagName: String) extends Exception(s"Missing tag: $tagName")