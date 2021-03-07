
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
