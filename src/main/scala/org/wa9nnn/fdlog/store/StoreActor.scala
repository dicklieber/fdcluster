
package org.wa9nnn.fdlog.store

import java.nio.file.Paths

import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.{CurrentStationProvider, DistributedQsoRecord, Qso}
import org.wa9nnn.fdlog.store.StoreActor.Dump
import org.wa9nnn.fdlog.store.network.{MultcastSenderActor, MulticastListenerActorXX}
class StoreActor(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider) extends Actor with LazyLogging {

  private val journal: String = context.system.settings.config.getString("fdlog.journalPath")
  private val store = new StoreMapImpl(nodeInfo, currentStationProvider, Some(Paths.get(journal)))

//  private val ourNode = AreWeAlone.isUs(nodeInfo.nodeAddress)
    private val ourNode = nodeInfo.nodeAddress

  private val senderActor: ActorRef = context.actorOf(MultcastSenderActor.props())
  context.actorOf(MulticastListenerActorXX.props(context.self), "MulticastListener")
  println(senderActor)

  override def receive: Receive = {
    case potentialQso: Qso ⇒
      val addresult = store.add(potentialQso)
      addresult match {
        case Added(addedQsoRecord) ⇒
          senderActor ! DistributedQsoRecord(addedQsoRecord, store.size)
        case unexpected ⇒
          println(s"Received: ${unexpected}")
      }
      sender ! addresult

    case Dump ⇒
      sender ! store.dump

    case d: DistributedQsoRecord ⇒
      val qsoRecord = d.qsoRecord
      val nodeAddress = qsoRecord.fdLogId.nodeAddress
      if (nodeAddress != ourNode) {
        logger.debug(s"Ingesting ${qsoRecord.qso} from ${nodeAddress}")
        store.addRecord(qsoRecord)
      } else {
        logger.debug(s"Ignoring our own QsoRecord: ${qsoRecord.qso}")
      }

  }
}

object StoreActor {

  case object Dump

}

