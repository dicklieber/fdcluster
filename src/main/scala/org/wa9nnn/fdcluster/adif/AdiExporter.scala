
package org.wa9nnn.fdcluster.adif

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{AdifExportRequest, QsoRecord}
import org.wa9nnn.util.{StructuredLogging, TimeHelpers}
import scalafx.collections.ObservableBuffer

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.util.{Try, Using}

class AdiExporter @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord], val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends StructuredLogging with RunningTask {
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
      allQsos.foreach { qso =>
        AdifQsoAdapter(qso.qso).entries.toSeq.sorted.foreach(adifentry =>
          print(adifentry))
        addOne()
        print(AdifResult.eor)
        print()
      }
    }
    done()
  }

}
