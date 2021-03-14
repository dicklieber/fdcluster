
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

package org.wa9nnn.fdcluster.javafx.runningtask

import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer, StyledText}
import org.wa9nnn.util.StructuredLogging
import java.time.{Duration, Instant}

/**
 * Something that runs in the background
 * that displays status in [[org.wa9nnn.fdcluster.javafx.entry.RunningTaskPane]]
 *
 * Implementations need to provide taskName and runningTaskInfoConsumer
 * call done when finished.
 */
trait RunningTask extends StructuredLogging{
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