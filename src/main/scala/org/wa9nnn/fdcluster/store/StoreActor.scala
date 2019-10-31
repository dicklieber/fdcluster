
package org.wa9nnn.fdcluster.store

import java.net.InetAddress
import java.nio.file.Path

import akka.actor.{Actor, ActorRef, Props}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.http.{ClientActor, FetchQsos}
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, ProgressStep, UuidsAtHost}
import org.wa9nnn.fdcluster.model.{Codec, CurrentStationProvider, DistributedQsoRecord, Qso, QsoRecord, QsosFromNode}
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState
import org.wa9nnn.fdcluster.store.network.{FdHour, MultcastSenderActor, MulticastListenerActor}
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.http.{ClientActor, FetchQsos}
import org.wa9nnn.fdcluster.store.StoreActor.{DumpCluster, DumpQsos}
import play.api.libs.json.Json
import scalafx.collections.ObservableBuffer
import org.wa9nnn.fdcluster.model.MessageFormats._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import org.wa9nnn.fdcluster.javafx.sync.StepsDataMethod.addStep

class StoreActor(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider,
                 inetAddress: InetAddress, config: Config,
                 journalPath: Option[Path],
                 allQsos: ObservableBuffer[QsoRecord],
                 stepsData: ObservableBuffer[ProgressStep]
                ) extends Actor with LazyLogging {

  private val store = new StoreMapImpl(nodeInfo, currentStationProvider, allQsos, stepsData, journalPath)
  private val clusterState = new ClusterState(nodeInfo.nodeAddress)
  implicit val timeout = Timeout(5 seconds)


  private val ourNode = nodeInfo.nodeAddress

  logger.info(s"StoreActor: ${self.path}")

  context.actorOf(MulticastListenerActor.props(inetAddress, config), "MulticastListener")
  private val senderActor: ActorRef = context.actorOf(MultcastSenderActor.props(config), "MulticastSender")
  private val clientActor = context.actorOf(ClientActor.props(stepsData))

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

    case RequestUuidsForHour(_, fdHours, _) ⇒
      val uuids = store.uuidForHours(fdHours.toSet)
      sender ! UuidsAtHost(nodeInfo.nodeAddress, uuids)

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

      clusterState.otherNodeWithMostThanUs() match {
        case Some(bestNode) ⇒
          stepsData.step("Best Node", bestNode)
          clientActor ! FetchQsos(bestNode)
        case None ⇒
          stepsData.step("No Best Node", "Done")
      }


    case qsosFromNode: QsosFromNode ⇒
      stepsData.step("Records", s"Received: ${qsosFromNode.size} qsos from ${qsosFromNode.nodeAddress}")
      logger.debug(syncMarker, s"got ${qsosFromNode.size}")
      store.merge(qsosFromNode.qsos)

    case ns: NodeStatus ⇒
      logger.trace(s"Got NodeStatus from ${ns.nodeAddress}")
      clusterState.update(ns)

    case DumpCluster ⇒
      sender ! clusterState.dump

    case DebugClearStore ⇒
      store.debugClear()

    case DebugKillRandom(nToKill) ⇒
      store.debugKillRandom(nToKill)

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
            allQsos: ObservableBuffer[QsoRecord],
            stepsData: ObservableBuffer[ProgressStep]): Props = {
    Props(new StoreActor(nodeInfo, currentStationProvider, inetAddress, config, Some(journalPath), allQsos, stepsData))
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

case class DebugKillRandom(nToKill: Int)