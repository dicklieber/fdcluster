package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.scene.control.Tab
import akka.actor.{ActorSystem, Cancellable}
import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX

import java.time.Duration
import scala.concurrent.ExecutionContextExecutor

/**
 * A [[Tab]] that automatically refreshes when in view (i.e. selected)
 */
trait AutoRefreshTab extends Tab with LazyLogging{
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
        initialDelay = Duration.ofSeconds(1),
        interval = Duration.ofSeconds(1),
        runnable = task,
        executor = executor)
      )
    } else {
      logger.trace("Cancel Timer")
      timer.foreach(_.cancel())
    }
  }


}
