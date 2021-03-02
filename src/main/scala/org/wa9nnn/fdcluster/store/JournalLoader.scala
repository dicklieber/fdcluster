
package org.wa9nnn.fdcluster.store

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsoRecord
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
 * Fills the "allQsos" ObsesrverabeBuffer from the journal.
 *
 * @param allQsos                 where to store.
 * @param journalFilePath         file.
 * @param runningTaskInfoConsumer progress UI
 */
class JournalLoaderImpl @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                                  @Named("journalPath") journalFilePath: Path,
                                  val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends RunningTask with JournalLoader {
  override def taskName: String = "Journal Loader"

  def run(): Future[BufferReady.type] = {
    Future {
      val typicalQsoLength = 413 // imperially determined by loading journal then divided file size by number of QSOs
      totalIterations = Files.size(journalFilePath) / typicalQsoLength
      val lineNumber = new AtomicInteger()
      val errorCount = new AtomicInteger()
      Using(Source.fromFile(journalFilePath.toUri)) { bufferedSource ⇒
        bufferedSource.getLines()
          .foreach { line: String ⇒
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
                        "qso" -> line
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
      //      logger.info(f"loaded $c%,d records in $d ($qsoPerSecond%.2f/per sec)")
      done()
      BufferReady
    }
  }

}

trait JournalLoader {
  def run(): Future[BufferReady.type]
}
