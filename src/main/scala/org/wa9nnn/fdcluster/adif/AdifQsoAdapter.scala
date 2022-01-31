
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

import org.wa9nnn.fdcluster._
import org.wa9nnn.fdcluster.contest.JournalFileNameSource
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.util.TimeHelpers.utcZoneId
import org.wa9nnn.util.UuidUtil.{fromBase64, toBase64}

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import java.time.{Instant, LocalDate, LocalTime, ZonedDateTime}
import java.util.UUID
import javax.inject.Inject
import scala.language.implicitConversions

class AdifQsoAdapter @Inject()(journalFileNameSource: JournalFileNameSource, nodeAddress: NodeAddress) {
  private val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

  /**
   * //todo error & various formats handling
   *
   * @param adif the Adif Qso
   * @return the FcCluster Qso
   * @throws MissingRequiredTag if required tag not found
   */
  def apply(adif: AdifQso): Qso = {
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
          case x: Throwable =>
            throw x
        }
      }

      def o(args: Any*): String = {
        val tagName = sc.parts.head
        try {
          map.getOrElse(tagName, "")
        } catch {
          case x: Throwable =>
            throw x
        }
      }
    }

    val bandMode = BandMode(
      bandName = m"BAND",
      modeName = m"MODE"
    )
    val exchange: Exchange = {
      val rr: Option[Exchange] = map.get("SRX_STRING").map {
        Exchange(_)
      }

      rr.getOrElse(
        Exchange(m"CLASS", m"ARRL_SECT")
      )

    }

    val stamp: Instant = {
      ZonedDateTime.of(
        LocalDate.parse(m"QSO_DATE", BASIC_ISO_DATE),
        LocalTime.parse(m"TIME_ON", timeFormat),
        utcZoneId).toInstant
    }
    Qso(callSign = m"CALL", exchange = exchange, bandMode = bandMode, qsoMetadata = QsoMetadata(
            operator = o"OPERATOR",
            rig = o"MY_RIG",
            ant = o"MY_ANTENNA",
            node = nodeAddress.displayWithIp,
            journal = journalFileNameSource.journalFileName,
            v = BuildInfo.canonicalVersion
          ), mHz =  Option(( m"FREQ".toFloat)), stamp = stamp, uuid = {
            try {
              fromBase64(m"APP_FDC_UUID")
            } catch {
              case _: MissingRequiredTag =>
                // APP_FDC_UUID is specific to adif files written by fdcluster
                // if missing, create one.
                UUID.randomUUID
            }
          })

  }

  def apply(qso: Qso): adif.AdifQso = {
    implicit def e(t2: (String, String)): AdifEntry = {
      AdifEntry(t2._1, t2._2)
    }

    val zdt = ZonedDateTime.ofInstant(qso.stamp, utcZoneId)
    val builder = new AdifQsoBuilder()
    builder("APP_FDC_UUID", toBase64(qso.uuid))
    builder("QSO_DATE", zdt.toLocalDate.format(BASIC_ISO_DATE))
    builder("TIME_ON", zdt.toLocalTime.format(timeFormat))
    builder("CALL", qso.callSign)
    builder("BAND", qso.bandMode.bandName)
    builder("MODE", qso.bandMode.modeName)
    builder("CLASS", qso.exchange.entryClass)
    builder("ARRL_SECT", qso.exchange.sectionCode)
    val qsoMetadata = qso.qsoMetadata
    builder("MY_RIG", qsoMetadata.rig)
    builder("MY_ANTENNA", qsoMetadata.ant)
    builder("OPERATOR", qsoMetadata.operator)


    builder.result
  }
}

class AdifQsoBuilder() {
  private val set = Set.newBuilder[AdifEntry]

  def apply(field: String, value: String): Unit = {
    if (value.nonEmpty) {
      set += AdifEntry(field, value)
    }
  }

  def result: AdifQso = {
    AdifQso(set.result())
  }
}

class MissingRequiredTag(tagName: String) extends Exception(s"Missing tag: $tagName")