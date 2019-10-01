
package org.wa9nnn.fdlog.store

import java.net.InetAddress
import java.nio.file.Path

import akka.actor.{Actor, ActorRef, Props}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.Markers.syncMarker
import org.wa9nnn.fdlog.javafx.sync.{StepsData, SyncDialog}
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.NodeStatus
import org.wa9nnn.fdlog.store.StoreActor.{DumpCluster, DumpQsos}
import org.wa9nnn.fdlog.store.network.cluster.{ClientActor, ClusterState, FetchQsos}
import org.wa9nnn.fdlog.store.network.{FdHour, MultcastSenderActor, MulticastListenerActor}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class StoreActor(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider,
                 inetAddress: InetAddress, config: Config, journalPath: Option[Path],
                 stepsData: StepsData) extends Actor with LazyLogging {

  private val store = new StoreMapImpl(nodeInfo, currentStationProvider, journalPath)
  private val clusterState = new ClusterState(nodeInfo.nodeAddress)
  implicit val timeout = Timeout(5 seconds)


  private val ourNode = nodeInfo.nodeAddress

  logger.info(s"StoreActor: ${self.path}")

  context.actorOf(MulticastListenerActor.props(inetAddress, config), "MulticastListener")
  private val senderActor: ActorRef = context.actorOf(MultcastSenderActor.props(config), "MulticastSender")
  private val clientActor = context.actorOf(Props[ClientActor])

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
      logger.debug(s"DumpQsos request")
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


    case Sync ⇒
      stepsData.step("Sync Request", "Start")

      clusterState.otherNodeWithMostThanUs().foreach {
        clientActor ! FetchQsos(_)
      }
    case records: Seq[QsoRecord] ⇒
      stepsData.step("Records", s"Received: ${records.size} qsos")
      logger.debug(syncMarker, s"got ${records.size}")
      store.merge(records)

    case ns: NodeStatus ⇒
      logger.trace(s"Got NodeStatus from ${ns.nodeAddress}")
      clusterState.update(ns)

    case DumpCluster ⇒
      sender ! clusterState.dump

    case DebugClearStore ⇒
      store.debugClear()

    case x ⇒
      println(s"Unexpected Message; $x")

  }

  override def postStop(): Unit = {
    logger.error("postStop: StoreActor")
    super.postStop()
  }

  override def postRestart(reason: Throwable): Unit

  = {
    logger.error("postRestart: StoreActor", reason)
    super.postRestart(reason)
  }
}

object StoreActor {

  case object DumpQsos

  case object DumpCluster


  def props(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, inetAddress: InetAddress, config: Config, journalPath: Path,
           stepsData:StepsData): Props = {
    Props(new StoreActor(nodeInfo, currentStationProvider, inetAddress, config, Some(journalPath), stepsData))
  }

}

case object Sync

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
case object DebugClearStore