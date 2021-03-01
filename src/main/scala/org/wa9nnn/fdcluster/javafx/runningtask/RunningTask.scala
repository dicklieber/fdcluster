
package org.wa9nnn.fdcluster.javafx.runningtask

import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer, StyledText}
import org.wa9nnn.util.JsonLogging
import java.time.{Duration, Instant}

/**
 * Something that runs in the background
 * that displays status in [[org.wa9nnn.fdcluster.javafx.entry.RunningTaskPane]]
 *
 * Implementations need to provide taskName and runningTaskInfoConsumer
 * call done when finished.
 */
trait RunningTask extends JsonLogging{
  def taskName: String
  val runningTaskInfoConsumer:RunningTaskInfoConsumer
  val start: Instant =  Instant.now()

  private var info: RunningTaskInfo = RunningTaskInfo(taskName)
   var totalIterations: Long = Int.MaxValue
   var n: Int = 0

  val timer = new java.util.Timer()
  timer.schedule(new java.util.TimerTask {
    def run(): Unit = {
      runningTaskInfoConsumer.update(info)
    }
  }, 250, 300)


  def addOne(): Unit = {
    n += 1
    val progressValue:Double = n.toDouble / totalIterations.toDouble
    val topLine = f"$n%,d of $totalIterations%,d"
    info = info(progressValue, StyledText(topLine))
    if(n == 1)
      runningTaskInfoConsumer.update(info)
  }

  def bottomLine(bottomLine:String):Unit = {
    info = info.copy(bottom = StyledText(bottomLine))
  }

  def done():Unit = {
    runningTaskInfoConsumer.update(info)
    logJson(taskName)
      .++("Duration" -> Duration.between(start, Instant.now()))
      .++("QSOs" -> n)
    timer.cancel()
    runningTaskInfoConsumer.done()
  }


}