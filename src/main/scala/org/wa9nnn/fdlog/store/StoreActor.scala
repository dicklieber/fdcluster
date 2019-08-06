
package org.wa9nnn.fdlog.store

import akka.actor.Actor
import org.wa9nnn.fdlog.model.Qso

class StoreActor(store:Store) extends Actor {
  println("Hello StoreActor")
  //  val store = new StoreMapImpl()

  override def receive: Receive = {
    case potentialQso: Qso ⇒

      sender ! store.add(potentialQso)
  }
}

