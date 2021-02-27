
package org.wa9nnn.util

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer, StyledText}
import org.wa9nnn.fdcluster.javafx.menu.BuildLoadRequest
import org.wa9nnn.fdcluster.model.{BandModeOperator, Exchange, Qso}

import java.nio.file.{Files, Paths}
import java.util.TimerTask
import scala.io.Source
import scala.util.control.Breaks._

object LaurelDbImporterTask {
  def apply(blr: BuildLoadRequest, runningTaskInfoConsumer: RunningTaskInfoConsumer)(f: Qso => Boolean): Unit = {
    var info = RunningTaskInfo("Loading Demo Laurel Data")

    val exchange = Exchange("3A", "IL")
    val stats = new DescriptiveStatistics()
    val path = Paths.get(blr.path)
    val filelength = Files.size(path)

    val t = new java.util.Timer()
    val task: TimerTask = new java.util.TimerTask {

      def run(): Unit = {
        runningTaskInfoConsumer.update(info)
      }
    }
    t.schedule(task, 250, 1000)
    var lineCount = 0

    val source = Source.fromFile(path.toFile)

    breakable {
      val lines = source.getLines
      lines.next()
      for (line <- lines) {
        stats.addValue(line.length)
        updateInfo()

        val cols = line.split(",").map(_.trim)
        val callsign = cols(1)
        val qso = Qso(
          callsign = callsign,
          bandMode = BandModeOperator(),
          exchange = exchange)
        if (!f(qso))
          break
        lineCount += 1
        if (lineCount == 1) {
          // 1st time, get display going.
          runningTaskInfoConsumer.update(info)
        }
        if (lineCount >= blr.max) {
          break
        }
      }
    }
    runningTaskInfoConsumer.done()
    task.cancel()

    def updateInfo(): Unit = {
      val qsoCountGuess = if (blr.max == 0)
        (filelength / stats.getMean).toLong
      else
        blr.max
      val n: Long = stats.getN
      val progressValue = n.toDouble / qsoCountGuess.toDouble
      val topLine = f"$n%,d of $qsoCountGuess%,d"
      info = info(progressValue, StyledText(topLine))
    }
  }

}


//object LaurelDbImporter extends JsonLogging {
//
//
//  private val exchange = Exchange("3A", "IL")
//
//  /**
//   * @param blr     specs for operation.
//   * @param f       function to do something with each [[Qso]]
//   */
//  def apply(blr: BuildLoadRequest)(f: Qso => Boolean): Unit = {
//    var lineCount = 0
//    val source = Source.fromURL(blr.url)
//    breakable {
//      val lines = source.getLines
//      lines.next()
//      for (line <- lines) {
//        val cols = line.split(",").map(_.trim)
//        val callsign = cols(1)
//        val qso = Qso(
//          callsign = callsign,
//          bandMode = BandModeOperator(),
//          exchange = exchange)
//        if (!f(qso))
//          break
//        lineCount += 1
//        if (lineCount >= blr.max) {
//          break
//        }
//      }
//    }
//  }
//
//}
