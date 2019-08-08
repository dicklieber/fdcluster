
package org.wa9nnn.fdlog.store

import akka.actor.Actor
import org.wa9nnn.fdlog.model.{CurrentStationProvider, Qso}
import org.wa9nnn.fdlog.store.StoreActor.Dump

class StoreActor(nodeInfo: NodeInfo,  currentStationProvider: CurrentStationProvider) extends Actor {
  val store = new StoreMapImpl(nodeInfo, currentStationProvider)
  override def receive: Receive = {
    case potentialQso: Qso ⇒
      sender ! store.add(potentialQso)

    case Dump ⇒
      sender ! store.dump
  }
}

object StoreActor{
  case object Dump
}