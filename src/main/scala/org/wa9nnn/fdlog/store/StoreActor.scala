
package org.wa9nnn.fdlog.store

import java.net.InetAddress
import java.nio.file.Path

import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.NodeStatus
import org.wa9nnn.fdlog.store.StoreActor.{DumpCluster, DumpQsos}
import org.wa9nnn.fdlog.store.network.{FdHour, MultcastSenderActor, MulticastListenerActor}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import org.wa9nnn.fdlog.store.network.cluster.ClusterState

class StoreActor(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, inetAddress: InetAddress, config: Config, journalPath: Option[Path]) extends Actor with LazyLogging {

  private val store = new StoreMapImpl(nodeInfo, currentStationProvider, journalPath)
  private val clusterState = new ClusterState(nodeInfo.nodeAddress)


  private val ourNode = nodeInfo.nodeAddress

  logger.info(s"StoreActor: ${self.path}")

  context.actorOf(MulticastListenerActor.props(inetAddress, config), "MulticastListener")
  private val senderActor: ActorRef = context.actorOf(MultcastSenderActor.props(config), "MulticastSender")

  context.system.scheduler.scheduleAtFixedRate(2 seconds, 17 seconds, self, StatusPing)

  override def receive: Receive = {
    case potentialQso: Qso ⇒
      val addResult: AddResult = store.add(potentialQso)
      addResult match {
        case Added(addedQsoRecord) ⇒
          val record = DistributedQsoRecord(addedQsoRecord, nodeInfo.nodeAddress, store.size)
          senderActor ! JsonContainer(record.getClass.getSimpleName, record)
        case unexpected ⇒
          println(s"Received: $unexpected")
      }
      sender ! addResult // send back to caller with all info allows UI to show what was recorded or dup

    case DumpQsos ⇒
      sender ! store.dump

    case d: DistributedQsoRecord ⇒
      val qsoRecord = d.qsoRecord
      val nodeAddress = qsoRecord.fdLogId.nodeAddress
      if (nodeAddress != ourNode) {
        logger.debug(s"Ingesting ${qsoRecord.qso} from $nodeAddress")
        store.addRecord(qsoRecord)
      } else {
        logger.debug(s"Ignoring our own QsoRecord: ${qsoRecord.qso}")
      }

    case fdHour: FdHour ⇒
      sender ! store.get(fdHour)

    case StatusPing ⇒
      val nodeStatus = store.nodeStatus
      senderActor ! JsonContainer(nodeStatus.getClass.getSimpleName, nodeStatus)

      val hoursToSync = clusterState.hoursToSync()

    case ns: NodeStatus ⇒
      logger.debug(s"Got NodeStatus")
      clusterState.update(ns)

    case DumpCluster ⇒
      sender ! clusterState.dump

    case x ⇒
      println(s"Unexpected Message; $x")

  }

  override def postStop(): Unit = {
    logger.error("postStop: StoreActor")
    super.postStop()
  }

  override def postRestart(reason: Throwable): Unit = {
    logger.error("postRestart: StoreActor", reason)
    super.postRestart(reason)
  }
}

object StoreActor {

  case object DumpQsos

  case object DumpCluster


  def props(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, inetAddress: InetAddress, config: Config, journalPath: Path): Props = {
    Props(new StoreActor(nodeInfo, currentStationProvider, inetAddress, config, Some(journalPath)))
  }

}

case class JsonContainer(className: String, json: String) extends Codec {
  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }

}

object JsonContainer {
  def apply(className: String, codec: Codec): JsonContainer = {
    val str = codec.toByteString.decodeString("UTF-8")
    JsonContainer(className, str)
  }
}

case object StatusPing