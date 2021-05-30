
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

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Names
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.adif.AdiExporter
import org.wa9nnn.fdcluster.cabrillo.{CabrilloExportRequest, CabrilloGenerator}
import org.wa9nnn.fdcluster.javafx.menu.ImportRequest
import org.wa9nnn.fdcluster.javafx.sync._
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.tools.{GenerateRandomQsos, RandomQsoGenerator}
import org.wa9nnn.util.ImportAdifTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class StoreActor(injector: Injector) extends Actor with LazyLogging with DefaultInstrumented {
  private implicit val timeout: Timeout = Timeout(5 seconds)

  val nodeAddress: NodeAddress = injector.instance[NodeAddress]
  val config: Config = injector.instance[Config]
  val randomQso: RandomQsoGenerator = injector.instance[RandomQsoGenerator]
  val multicastSender: ActorRef = injector.instance[ActorRef](Names.named("multicastSender"))
  val store: StoreLogic = injector.instance[StoreLogic]
  val clusterControl: NetworkControl = injector.instance[NetworkControl]

  logger.info(s"StoreActor: ${self.path}")

  context.system.scheduler.scheduleAtFixedRate(2 seconds, 7 seconds, self, StatusPing)

  //  journalLoader().pipeTo(self)

  override def receive: Receive = {
    /**
     * Finish up sync with data from another node
     */
    case QsosFromNode(qsos) =>
      logger.debug(syncMarker, s"got ${qsos.size}")
      qsos.foreach(store.ingestAndPersist)


    case BufferReady =>
      clusterControl.up()

    case potentialQso: Qso =>

      val triedQso = store.ingestAndPersist(potentialQso)
      triedQso.foreach { qso =>
        val record = DistributedQso(qso, nodeAddress)
        multicastSender ! JsonContainer(record)
      }

      sender ! AddResult(triedQso)

    case request: RequestUuidsForHour =>
      val uuids: List[Uuid] = store.uuidForHour(request.fdHour)
      sender ! UuidsAtHost(nodeAddress, uuids) //to asking host.

    case uuidsAtHost: UuidsAtHost =>
      logger.debug(uuidsAtHost.toString)
      val missing: Iterator[Uuid] = store.filterAlreadyPresent(uuidsAtHost.iterator)
      val requestQsosForUuids = RequestQsosForUuids(missing.toList)
      logger.debug(requestQsosForUuids.toString)
      sender ! requestQsosForUuids // send to cluster on this host

    case rqfu: RequestQsosForUuids =>
      logger.debug(rqfu.toString)

      val qsos: List[Qso] = rqfu.uuids.flatMap(uuid =>
        store.get(uuid)
      )
      val qsosFromNode = QsosFromNode(qsos)
      logger.debug(qsosFromNode.toString)
      sender ! qsosFromNode


    case rqfh: RequestQsosForHour =>
      val qsos: List[Qso] = store.getQsos(rqfh.fdHour)
      val qsosFromNode = QsosFromNode(qsos)
      logger.debug(qsosFromNode.toString)
      sender ! qsosFromNode

    case qc: QsosFromNode =>
      qc.qsos.foreach(store.ingest)
      logger.error(s"Got to so-called unreachable code!!!!!!!")


    case d: DistributedQso ⇒
      val qso = d.qso
      val remoteNodeAddress = qso.qsoMetadata.node
      if (remoteNodeAddress != nodeAddress) {
        logger.debug(s"Ingesting $qso from $remoteNodeAddress")
        store.ingestAndPersist(qso)
      } else {
        logger.debug(s"Ignoring our own Qso: $qso")
      }

    case fdHour: FdHour ⇒
      sender ! store.get(fdHour)

    case StatusPing ⇒
      store.sendNodeStatus()


    case ClearStore =>
      store.clear()

    case DebugKillRandom(nToKill) =>
      store.debugKillRandom(nToKill)

    case cer: CabrilloExportRequest =>
      val cabrilloGenerator: CabrilloGenerator = injector.instance[CabrilloGenerator]
      cabrilloGenerator(cer)

    case exportRequest: AdifExportRequest =>
      val exporter: AdiExporter = injector.instance[AdiExporter]
      exporter(exportRequest)

    case ImportRequest(path) =>
      val importTask = injector.instance[ImportAdifTask]
      importTask(path, store)

    case search: Search =>
      val origSender = sender()
      val searchResult: SearchResult = store.search(search)
//      sender ! searchResult
      origSender ! searchResult

    case gr: GenerateRandomQsos =>
      randomQso(gr) {
        qso =>
          store.ingestAndPersist(qso)
      }

    case scala.util.Failure(e)
    =>
      logger.error("Unexpected Failure", e)

    case Failure(e)
    =>
      logger.error("Unexpected Failure", e)

    case x ⇒
      logger.error(s"Unexpected Message; $x")

  }


  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    try {
      super.aroundReceive(receive, msg)
    } catch {
      case e: Exception =>
        logger.error("StoreActor", e)
    }
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


case object DumpCluster

case object StatusPing

case object ClearStore

case class DebugKillRandom(nToKill: Int)

case object BufferReady


case class Search(partial: String, bandMode: BandMode, max: Int = 15)

case class SearchResult(qsos: Seq[Qso], fullCount: Int, search:Search) {
  def display(): String = {
    val length = qsos.length
    if (length < fullCount)
      f"$length%,d of $fullCount%,d"
    else
      ""
  }
}
