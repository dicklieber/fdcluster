
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

package org.wa9nnn.fdcluster.dupsheet

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{ContestProperty, QsoRecord}
import org.wa9nnn.util.StructuredLogging
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.io.PrintWriter
import javax.inject.{Inject, Singleton}
@Singleton
class GenerateDupSheet @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                                 contestProperty:ContestProperty) extends StructuredLogging {
  /**
   *
   * @param pw where to write to.
   * @return number of QSOs writer to Dup.
   */
  def apply(pw: PrintWriter): Int = {
    val contest = contestProperty.value
    pw.print(s"Call Used: ${contest.callSign}  Class: ${contest.ourExchange.entryClass}  ARRL Section: ${contest.ourExchange.sectionCode}\r\n")
    pw.print("\r\n")
    pw.print("Dupe Sheet\r\n")

    var count = 0
    allQsos.groupBy(qsoRecord =>
      qsoRecord.qso.bandMode)
      .foreach { case (bandMode, ob) =>
        val head = s"$bandMode"
        pw.print("\r\n" + head + "\r\n")
        val callSigns = ob.map((qsoRecord: QsoRecord) =>
          qsoRecord.qso.callSign
        )
        callSigns.sorted.foreach(cs => {
          pw.print(s"$cs\r\n")
          count += 1
        })
        pw.print(f"\r\n$head  Total Contacts ${callSigns.size}%,d\r\n")
      }
    count
  }
}
