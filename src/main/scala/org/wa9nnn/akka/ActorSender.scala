package org.wa9nnn.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.reflect.ClassTag

trait ActorSender {
  val actor:ActorRef
  private implicit val timeout: Timeout = Timeout(65, TimeUnit.SECONDS)

  def !(message: Any): Unit = {
    actor ! message
  }
  def ? [T:ClassTag](message: Any):Future[T] = {
    (actor ? message).mapTo[T]
  }

}
