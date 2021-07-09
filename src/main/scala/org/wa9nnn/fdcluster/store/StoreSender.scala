package org.wa9nnn.fdcluster.store

import akka.actor.ActorRef
import com.google.inject.Injector
import com.sandinh.akuice.ActorInject
import org.wa9nnn.akka.ActorSender

import javax.inject.{Inject, Singleton}

/**
 * Convenient way for non-actor to send a message to the Store
 * Easy to mock.
 */
@Singleton
class StoreSender @Inject()(implicit val injector: Injector) extends ActorInject with ActorSender {
  val actor: ActorRef = injectTopActor[StoreActor]("storeActor")
}
