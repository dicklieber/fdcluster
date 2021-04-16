
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

package org.wa9nnn.fdcluster.javafx.sync

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricName, Timer}
import org.wa9nnn.util.StructuredLogging
import scalafx.collections.ObservableBuffer

@Singleton
class SyncSteps extends DefaultInstrumented  with StructuredLogging{
  override lazy val metricBaseName: MetricName = MetricName("Sync")

  private val syncTimer: Timer = metrics.timer("Sync")
  val observableBuffer: ObservableBuffer[ProgressStep] = ObservableBuffer[ProgressStep](Seq.empty)

  private var startStamp: Option[Instant] = None

  def step(name: String, result: Any): Unit = {
    val s = result match {
      case s: String ⇒ s
      case i: Int ⇒ f"$i%,d"
      case x ⇒ x.toString
    }
    val step = ProgressStep(name, s)
    observableBuffer += step
  }

  def start(): Unit = {
    observableBuffer.clear()
    step("Start", "")
    if(startStamp.isDefined)
      logger.error("Starting already stated sync operation.")
    startStamp = Some(Instant.now)
  }

  def finish(name: String, result: Any): Unit = {
    step(name, result)
    startStamp.foreach { start ⇒
      startStamp = None
      syncTimer.update(Duration.between(start, Instant.now()).toMillis, TimeUnit.MILLISECONDS)
    }
  }
}

case class ProgressStep(name: String, result: String, start: Instant = Instant.now)