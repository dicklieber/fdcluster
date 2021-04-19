package org.wa9nnn.fdcluster.javafx.entry

import akka.actor.{ActorSystem, Cancellable}
import org.scalafx.extras.onFX
import org.wa9nnn.util.StructuredLogging
import scalafx.scene.control.Tab

import java.time.Duration
import scala.concurrent.ExecutionContextExecutor

/**
 * A [[Tab]] that automatically refreshes when in view (i.e. selected)
 */
trait AutoRefreshTab extends Tab with StructuredLogging{
  // Subclass imlementes this to refresh the daga displayed
  def refresh():Unit
  val actorSystem:ActorSystem
  implicit val executor: ExecutionContextExecutor = actorSystem.dispatcher

  private val task = new Runnable {
    def run() {
      logger.trace("Refresh:scheduled")
      onFX {
        refresh()
      }
    }
  }
  var timer: Option[Cancellable] = None

  selected.onChange { (_, _, isSelectd) =>
    if (isSelectd) {
      logger.trace("Refresh:selected")
      refresh()
      timer = Some(actorSystem.scheduler.scheduleAtFixedRate(
        initialDelay = Duration.ofSeconds(5),
        interval = Duration.ofSeconds(5),
        runnable = task,
        executor = executor)
      )
    } else {
      logger.trace("Cancel Timer")
      timer.foreach(_.cancel())
    }
  }


}
