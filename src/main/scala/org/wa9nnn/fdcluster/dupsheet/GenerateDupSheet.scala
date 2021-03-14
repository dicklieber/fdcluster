
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
import org.wa9nnn.fdcluster.model.{OurStationStore, QsoRecord}
import org.wa9nnn.util.StructuredLogging
import scalafx.collections.ObservableBuffer

import java.io.PrintWriter
import javax.inject.Inject

class GenerateDupSheet @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                                 ourStationStore: OurStationStore) extends StructuredLogging {

  def apply(pw: PrintWriter): Unit = {
    val ourStation = ourStationStore.value
    val exchange = ourStation.exchange
    pw.print(s"Call Used: ${ourStation.ourCallsign}  Class: ${exchange.entryClass}  ARRL Section: ${exchange.section}\r\n")
    pw.print("\r\n")
    pw.print("Dupe Sheet\r\n")

    allQsos.groupBy(qsoRecord =>
      qsoRecord.qso.bandMode.bandMode)
      .foreach { case (bandMode, ob) => {
        val head = s"$bandMode"
        pw.print(head + "\r\n")
        val callSigns = ob.map(qsoRecord =>
          qsoRecord.qso.callsign
        )
        callSigns.foreach(cs => pw.print(s"$cs\r\n"))
        pw.print(s"$head  Total Contacts ${callSigns.size}\r\n")
      }
      }

  }
}
