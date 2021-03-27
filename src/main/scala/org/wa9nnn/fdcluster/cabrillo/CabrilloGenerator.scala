
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

package org.wa9nnn.fdcluster.cabrillo

import com.google.inject.name.Named
import com.wa9nnn.cabrillo.model.{SimpleTagValue, TagValue}
import com.wa9nnn.cabrillo.{Builder, CabrilloWriter}
import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.cabrillo.QsoFD.cabrilloDtFormat
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{ContestProperty, QsoRecord, Qso => fdQso}
import org.wa9nnn.util.TimeHelpers
import scalafx.collections.ObservableBuffer

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.util.{Failure, Success, Using}

class CabrilloGenerator @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                                  val runningTaskInfoConsumer: RunningTaskInfoConsumer,
                                  contestProperty: ContestProperty) extends RunningTask {
  override def taskName: String = "Cabrillo Generator"



  def apply(cabrilloExportRequest: CabrilloExportRequest): Unit = {
    val builder = new Builder()
    builder + ("CREATED-BY", s"${BuildInfo.name} ${BuildInfo.version}")
    builder + ("CALLSIGN", contestProperty.callSign)
    builder + ("CONTEST", contestProperty.event)

    cabrilloExportRequest.cabrilloValues.fieldValues.foreach { cv =>
      builder.+(cv.tagValue)
    }
    allQsos.foreach(qsoRecord => {
      builder + qso(qsoRecord.qso)
      addOne()
    }
    )
    val data = builder.toCabrilloData

    val path = Paths.get(cabrilloExportRequest.directory).resolve(cabrilloExportRequest.fileName)
    Using(new PrintWriter(Files.newBufferedWriter(path))) { printWriter =>
      CabrilloWriter.write(data, printWriter)
      data.lineCount
    } match {
      case Failure(exception) =>
        logger.error("Failed to write Cabrillo file!", exception)
      case Success(count) =>
        logger.info(f"Wrote $count%,d lines to $path.")
    }

    done()
  }


  def qso(q: fdQso): TagValue = {
    val band = q.bandMode.bandName //todo map to cabrillo
    val mode = q.bandMode.modeName //todo map to cabrillo?
    val datetime = ZonedDateTime.ofInstant(q.stamp, TimeHelpers.utcZoneId).format(cabrilloDtFormat)
    val ourCallSign = contestProperty.callSign
    val entryClass = contestProperty.ourExchange.entryClass
    val section = contestProperty.ourExchange.sectionCode
    //QSO: 14000 PH 2020-06-28 1647 WA9NNN        1D  IL     WA0ARM        1D  KS
    val body = f"$band%-6s $mode $datetime $ourCallSign%-13s $entryClass%3s $section%3s     ${q.callSign}%-13s ${q.exchange.entryClass}%3s ${q.exchange.sectionCode}%3s"
    SimpleTagValue("QSO", 0, body)
  }
}

object QsoFD {
  val cabrilloDtFormat: DateTimeFormatter = DateTimeFormatter ofPattern "yyyy-MM-dd HHmm"

}
  
