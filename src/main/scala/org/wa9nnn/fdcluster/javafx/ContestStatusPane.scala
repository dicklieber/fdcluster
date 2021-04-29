package org.wa9nnn.fdcluster.javafx

import akka.actor.ActorSystem
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.AllContestRules
import _root_.scalafx.scene.control.Label
import _root_.scalafx.scene.layout.HBox

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ContestStatusPane @Inject()(actorSystem: ActorSystem, allContestRules: AllContestRules) extends HBox with Runnable {
  actorSystem.getScheduler.scheduleWithFixedDelay(
    initialDelay = 2 seconds,
    delay = 275 milliseconds)(this)

  private val label: Label = new Label("--")

  children = Seq(label)

  override def run(): Unit = {
    onFX(
      allContestRules.scheduleMessage.applyTo(label)
    )
  }
}
