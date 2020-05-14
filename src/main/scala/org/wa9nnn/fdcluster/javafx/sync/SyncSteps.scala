
package org.wa9nnn.fdcluster.javafx.sync

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

import javax.inject.Singleton
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricName, Timer}
import scalafx.collections.ObservableBuffer

@Singleton
class SyncSteps extends DefaultInstrumented {
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
    assert(startStamp.isEmpty, "starting already stated sync operation.")
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