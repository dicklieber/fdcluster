
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

package org.wa9nnn.fdcluster.store

import com.google.inject.name.Named
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.wa9nnn.fdcluster.{FileManager, QsoCountCollector}
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsoRecord
import org.wa9nnn.util.BoolConverter.s2b
import play.api.libs.json.Json
import scalafx.collections.ObservableBuffer

import java.nio.file.{Files, Path}
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.Using

/**
 * Fills the "allQsos" from the journal.
 * Runs at startup only.
 * Talks directly to allQsos which can only be done at startup time.
 *
 * @param allQsos                 where to store.
 * @param fileManager             where to find files..
 * @param runningTaskInfoConsumer progress UI
 */
class JournalLoader @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                              fileManager: FileManager,
                              qsoStatCollector:QsoCountCollector,
                              val runningTaskInfoConsumer: RunningTaskInfoConsumer) {
  def apply(): Future[BufferReady.type] = {
    new Task(runningTaskInfoConsumer)()
  }


  class Task(val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends RunningTask {

    override def taskName: String = "Journal Loader"

    val journalFilePath: Path = fileManager.journalFile

    def apply(): Future[BufferReady.type] = {
      Future {
        val skipJournal: Boolean = System.getProperty("skipJournal", "false")
        if (skipJournal) {
          logger.info("skipJournal")
        }
        if (Files.size(journalFilePath) > 0 && !skipJournal) {
          val qsoLineLengths = new SummaryStatistics()
          val typicalQsoLength = 363 // empirically determined by loading journal then divided file size by number of QSOs
          // see log message with meanLineLength to get latest.
          totalIterations = Files.size(journalFilePath) / typicalQsoLength
          val lineNumber = new AtomicInteger()
          val errorCount = new AtomicInteger()
          Using(Source.fromFile(journalFilePath.toUri)) { bufferedSource ⇒
            bufferedSource.getLines()
              .foreach { line: String ⇒
                qsoLineLengths.addValue(line.length)
                lineNumber.incrementAndGet()
                try {
                  addOne()
                  val qsoRecord = Json.parse(line).as[QsoRecord]
                  allQsos.addOne(qsoRecord) // todo consider batching up and using addAll instead of addOne

                } catch {
                  case e: Exception =>
                    val err = errorCount.incrementAndGet()
                    err match {
                      case 25 =>
                        logger.error("More than 25 errors, stopping logging!")
                      case x if x < 25 =>
                        logJson("Journal Error")
                          .++("line" -> lineNumber.get,
                            "error" -> e.getClass.getName,
                            "qso" -> line,
                          )
                          .error()
                    }
                }
              }
          }
          if (errorCount.get > 0) {
            logger.info(f"${errorCount.get}%,d lines with errors in $journalFilePath")
          }
          val duration = Duration.between(start, Instant.now())
          val d: String = org.wa9nnn.util.TimeHelpers.durationToString(duration)
          val c: Int = lineNumber.get()
          val qsoPerSecond: Double = c.toDouble / duration.getSeconds.toDouble
          logger.info(f"loaded $c%,d records in $d ($qsoPerSecond%.2f/per sec)")
          logJson("load journal")
            .++(
              "qsos" -> c,
              "qsoPerSecond" -> qsoPerSecond,
              "meanLineLength" -> qsoLineLengths.getMean,
              "journal" -> journalFilePath
            )
            .info()
        } else {
          // no journal file
          logger.info(s"no journal at $journalFilePath")
        }
        done()
        BufferReady
      }
    }
  }
}
