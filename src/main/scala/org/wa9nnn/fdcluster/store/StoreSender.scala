package org.wa9nnn.fdcluster.store

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.reflect.ClassTag

/**
 * Convenient way for non-actor to send a message to the Store
 * Easy to mock.
 */
@Singleton
class StoreSender @Inject()(@Named("store") store: ActorRef) {
  private implicit val timeout: Timeout = Timeout(65, TimeUnit.SECONDS)

  def !(message: Any): Unit = {
    store ! message
  }
  def ? [T:ClassTag](message: Any):Future[T] = {
    (store ? message).mapTo[T]
  }
}
