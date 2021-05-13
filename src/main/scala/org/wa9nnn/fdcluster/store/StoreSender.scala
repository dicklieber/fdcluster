package org.wa9nnn.fdcluster.store

import akka.actor.ActorRef
import com.google.inject.name.Named

import javax.inject.{Inject, Singleton}

/**
 * Convenient way for non-actor to send a message to the Store
 * Easy to mock.
 */
@Singleton
class StoreSender @Inject()(@Named("store") store: ActorRef) {
  def !(message: Any): Unit = {
    store ! message
  }
}
