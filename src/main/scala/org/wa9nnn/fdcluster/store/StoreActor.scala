
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.store

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import akka.util.{ByteString, Timeout}
import com.google.inject.Injector
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.adif.AdiExporter
import org.wa9nnn.fdcluster.cabrillo.{CabrilloExportRequest, CabrilloGenerator}
import org.wa9nnn.fdcluster.http.{ClientActor, FetchQsos}
import org.wa9nnn.fdcluster.javafx.menu.{BuildLoadRequest, ImportRequest}
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, SyncSteps, UuidsAtHost}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState
import org.wa9nnn.fdcluster.store.network.{FdHour, MultcastSenderActor, MulticastListenerActor}
import org.wa9nnn.fdcluster.tools.{GenerateRandomQsos, RandomQsoGenerator}
import org.wa9nnn.util.{ImportTask, LaurelDbImporterTask}
import play.api.libs.json.Json

import java.net.InetAddress
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class StoreActor(injector: Injector,
                 nodeInfo: NodeInfo,
                 inetAddress: InetAddress, config: Config,
                 syncSteps: SyncSteps,
                 store: StoreMapImpl,
                 journalLoader: JournalLoader,
                 randomQso: RandomQsoGenerator
                ) extends Actor with LazyLogging with DefaultInstrumented {
  private val clusterState = new ClusterState(nodeInfo.nodeAddress)
  private implicit val timeout: Timeout = Timeout(5 seconds)


  private val ourNode = nodeInfo.nodeAddress

  logger.info(s"StoreActor: ${self.path}")

  context.actorOf(MulticastListenerActor.props(inetAddress, config), "MulticastListener")
  private val senderActor: ActorRef = context.actorOf(MultcastSenderActor.props(config), "MulticastSender")
  private val clientActor = context.actorOf(ClientActor.props(syncSteps))

  context.system.scheduler.scheduleAtFixedRate(2 seconds, 17 seconds, self, StatusPing)

  journalLoader().pipeTo(self)

  override def receive: Receive = {
    case BufferReady =>
      //todo load local indices
      store.loadLocalIndicies()
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

    /**
     * Start a sync operation
     */
    case Sync ⇒
      syncSteps.step("Sync Request", "Start")

      clusterState.otherNodeWithMostThanUs() match {
        case Some(bestNode) ⇒
          syncSteps.step("Best Node", bestNode)
          clientActor ! FetchQsos(bestNode)
        case None ⇒
          syncSteps.step("No Best Node", "Done")
      }

    /**
     * Finish up sync with data from another node
     */
    case qsosFromNode: QsosFromNode ⇒
      syncSteps.step("Records", s"Received: ${qsosFromNode.size} qsos from ${qsosFromNode.nodeAddress}")
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

    case blr: BuildLoadRequest =>
      val laurelDbImporterTask = injector.instance[LaurelDbImporterTask]
      laurelDbImporterTask(blr)

    case cer: CabrilloExportRequest =>
      val cabrilloGenerator: CabrilloGenerator = injector.instance[CabrilloGenerator]
      cabrilloGenerator(cer)

    case exportRequest: AdifExportRequest =>
      val exporter: AdiExporter = injector.instance[AdiExporter]
      exporter(exportRequest)

    case ImportRequest(path) =>
      val importTask = injector.instance[ImportTask]
      importTask(path)

    case search: Search =>
      sender ! store.search(search)

    case gr: GenerateRandomQsos =>
      randomQso(gr) {
        qso =>
          store.add(qso)
      }

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

case object DumpQsos

case object DumpCluster

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

case object BufferReady


case class Search(partial: String, bandMode: BandMode, max: Int = 15)

case class SearchResult(qsos: Seq[QsoRecord], fullCount: Int) {
  def display(): String = {
    val length = qsos.length
    if (length < fullCount)
      f"$length%,d of $fullCount%,d"
    else
      ""
  }
}
