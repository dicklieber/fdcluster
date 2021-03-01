
package org.wa9nnn.util

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.menu.BuildLoadRequest
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{BandModeOperator, Exchange, Qso}
import org.wa9nnn.fdcluster.store.{Added, Dup, Store}

import java.nio.file.{Files, Paths}
import javax.inject.Inject
import scala.io.Source
import scala.util.control.Breaks._

/**
 * Onu invoke from[[org.wa9nnn.fdcluster.store.StoreActor]]
 *
 * @param store
 * @param runningTaskInfoConsumer
 */
class LaurelDbImporterTask @Inject()(store: Store, val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends RunningTask {
  override val taskName: String = "Loading Demo Laurel Data"

  def apply(blr: BuildLoadRequest) {
    val exchange = Exchange("3A", "IL")
    val stats = new DescriptiveStatistics()
    val path = Paths.get(blr.path)
    var dupCount = 0

    val source = Source.fromFile(path.toFile)
    val typicalRowLength = 28L

    totalIterations =
      if (blr.max > 0)
        blr.max
      else
        Files.size(path) / typicalRowLength


    breakable {
      val lines = source.getLines
      lines.next()
      for (line <- lines) {
        stats.addValue(line.length)
        addOne()

        val cols = line.split(",").map(_.trim)
        val callsign = cols(1)
        val qso = Qso(
          callsign = callsign,
          bandMode = BandModeOperator(),
          exchange = exchange)
        store.add(qso) match {
          case Added(_) =>
          case Dup(_) =>
            dupCount += 1
            bottomLine(f"Dups: $dupCount%,d")
        }
        if (n >= blr.max) {
          break
        }
      }
    }
    done()
  }
}


