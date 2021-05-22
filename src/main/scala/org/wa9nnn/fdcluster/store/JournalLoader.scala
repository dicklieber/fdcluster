
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

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.wa9nnn.fdcluster.contest.JournalProperty
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.Qso
import org.wa9nnn.util.BoolConverter.s2b
import play.api.libs.json.Json

import java.nio.file.{Files, Path}
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import scala.io.Source
import scala.util.Using

/**
 * Fills the "allQsos" from the journal.
 * Runs at startup only.
 * Talks directly to allQsos which can only be done at startup time.
 *
 * @param journalManager          access to journal
 * @param runningTaskInfoConsumer progress UI
 */
class JournalLoader @Inject()(
                              journalManager: JournalProperty,
                              val runningTaskInfoConsumer: RunningTaskInfoConsumer) {
  def startLoad(f:( Qso) => Unit): Unit = {
    new Task(runningTaskInfoConsumer)(f)

  }

  class Task(val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends RunningTask with LazyLogging {

    override def taskName: String = "Journal Loader"

    def apply(qsoAdder:( Qso) => Unit): Unit = {
      try {
        if (System.getProperty("skipJournal", "false")) throw new SkipJournal
        journalManager.journalFilePathProperty.value.foreach { journalFilePath =>
          val typicalQsoLength = 250 // empirically determined by loading journal then divided file size by number of QSOs
          // see log message with meanLineLength to get latest.
          totalIterations = {
            if (Files.exists(journalFilePath))
              Files.size(journalFilePath) / typicalQsoLength
            else
              0
          }
          processLines(journalFilePath, qsoAdder)
        }
      }
      catch {
        case e: JournalException =>
          e.log()
      }
      finally {
        done()
      }
    }

    private def processLines(journalFilePath: Path, qsoAdder:( Qso) => Unit) = {
      val qsoLineLengths = new SummaryStatistics()
      val lineNumber = new AtomicInteger()
      val errorCount = new AtomicInteger()
      Using(Source.fromFile(journalFilePath.toUri)) { bufferedSource ⇒
        bufferedSource.getLines()
          .foreach { line: String ⇒
            qsoLineLengths.addValue(line.length)
            lineNumber.incrementAndGet()
            try {
              if (line.startsWith("{\"jour")) {
                //todo check header
              } else {
                countOne()
                val Qso = Json.parse(line).as[Qso]
                qsoAdder(Qso)
              }
            }
            catch {
              case e: Exception =>
                val err = errorCount.incrementAndGet()
                err match {
                  case 25 =>
                    logger.error("More than 25 errors, stopping logging!")
                  case x if x < 25 =>
                    logger.error(s"Journal Error line:${lineNumber.get} error: ${e.getClass.getName} qso: $line")
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
        logger.info(f"loaded $c%,d records in $d ($qsoPerSecond%.2f/per sec) meanLineLength: ${qsoLineLengths.getMean}")
      }
    }
  }

  abstract class JournalException(message: String) extends Exception(message) with LazyLogging {
    def log(): Unit = {
      logger.warn(message)
    }
  }

  case class NoJournalDefined() extends JournalException("No journal defined")

  case class EmptyJournal(file: Path) extends JournalException(s"journal: $file does not exist!")

  case class SkipJournal() extends JournalException("skipping journal because of -DskipJournal=true")


}
