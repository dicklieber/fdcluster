
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

import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{AdifExportRequest, QsoRecord}
import org.wa9nnn.fdcluster.store.QsoSource
import org.wa9nnn.util.{StructuredLogging, TimeHelpers}

import java.io.PrintWriter
import java.nio.file.Files
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.util.{Try, Using}

class AdiExporter @Inject()(qsoSource: QsoSource, val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends StructuredLogging with RunningTask {
  val taskName = "Export ADIF"

  private def print(s: String = "")(implicit writer: PrintWriter): Unit = {
    writer.println(s"$s\r\n")
  }

  private def print(tag: String, value: String)(implicit writer: PrintWriter): Unit = {
    print(AdifEntry(tag, value))
  }

  private def print(adif: AdifResult)(implicit writer: PrintWriter): Unit = {
    writer.print(adif.toLine)
  }

  def apply(exportRequest: AdifExportRequest): Unit = {

    val r: Try[Unit] = Using {
      val path = exportRequest.exportFile.path
      new PrintWriter(Files.newBufferedWriter(path))
    } { implicit writer =>

      // header
      print("Field Day Cluster Logger")
      print("\tby Dick Lieber WA9NNN")
      print(s"\tLog exported on: ${Instant.now}")
      print("ADIF_VER", "3.1.1")
      print("CREATED_TIMESTAMP", Instant.now().atZone(TimeHelpers.utcZoneId).format(DateTimeFormatter.BASIC_ISO_DATE))
      print("PROGRAMID", "Field Day Cluster Logger")
      print("PROGRAMVERSION", BuildInfo.version)
      print(AdifResult.eoh)
      print("")


      //records
      qsoSource.qsoIterator.foreach { qso: QsoRecord =>
        AdifQsoAdapter(qso).entries.toSeq.sorted.foreach(adifentry =>
          print(adifentry))
        countOne()
        print(AdifResult.eor)
        print()
      }
    }
    done()
  }

}
