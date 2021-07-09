package org.wa9nnn.fdcluster.model.sync

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Named
import com.sandinh.akuice.ActorInject
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.akka.ActorSender
import org.wa9nnn.fdcluster.contest.JournalProperty
import org.wa9nnn.fdcluster.http.HttpClientActor
import org.wa9nnn.fdcluster.javafx.cluster.{ClusterTable, FdHours, NodeHistory}
import org.wa9nnn.fdcluster.javafx.sync._
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{ContestProperty, NodeAddress}
import org.wa9nnn.fdcluster.store.StoreSender

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Handles [[NodeStatus]] messages from all nodes, including our own.
 */
class ClusterActor @Inject()(nodeAddress: NodeAddress,
                             val injector: Injector,
                             store: StoreSender,
                             @Named("nodeStatusQueue") nodeStatusQueue: ActorRef,
                             contestProperty: ContestProperty,
                             journalProperty: JournalProperty,
                             clusterTable: ClusterTable,
                             fdHours: FdHours,
                             nodeHistory: NodeHistory,
                            ) extends Actor with LazyLogging with ActorInject {
  logger.info("Starting ClusterActor")
  private implicit val timeout: Timeout = Timeout(5 seconds)
  context.system.scheduler.scheduleAtFixedRate(17 seconds, 17 seconds, self, Purge)
  private val httpClient: ActorRef = injectActor[HttpClientActor]
  private val heartBeatMap = new TrieMap[NodeAddress, HeartBeatMessage]()
  private var ourNodeStatus: NodeStatus = NodeStatus(BaseNodeStatus(nodeAddress = nodeAddress))

  override def receive: Receive = {

    case s: SendContainer => httpClient ! s

    case hb: HeartBeatMessage =>
      logger.debug(s"Got: $hb")
      // if no HB or digest is different then as for NodeStatus via HTTP.
      if (hb.needNodeStatus(heartBeatMap.get(hb.nodeAddress))) {
        heartBeatMap.put(hb.nodeAddress, hb)
        httpClient ! SendContainer(NodeStatusRequest(), hb.nodeAddress)
      }

    case ns: NodeStatus ⇒
      val theirNodeAddress = ns.nodeAddress
      logger.trace(s"Got NodeStatus from $theirNodeAddress")
      nodeHistory(ns.nodeAddress)
      fdHours.update(ns)
      clusterTable.update(ns)
      if (theirNodeAddress == nodeAddress) {
        ourNodeStatus = ns
      } else {
        ns.journal.foreach(journal => journalProperty.update(journal))
        ns.contest.foreach(contest => contestProperty.update(contest))
        nodeStatusQueue ! ns
        (nodeStatusQueue ? NextNodeStatus).mapTo[Option[NodeStatus]].map {
          {
            _.map { ns =>
              val messages = ns.qsoHourDigests.flatMap { otherQsoHourDigest: QsoHourDigest =>
                val fdHour = otherQsoHourDigest.fdHour
                ourNodeStatus.nodeStatus.digestForHour(fdHour) match {
                  case Some(ourQsoHourDigest: QsoHourDigest) =>
                    if (ourQsoHourDigest.digest == otherQsoHourDigest.digest) {
                      // we match them, nothing to do
                      Seq.empty

                    } else {
                      Seq(SendContainer(RequestUuidsForHour(fdHour, ns.nodeAddress, nodeAddress, getClass), ns.nodeAddress))
                    }
                  case None => // we dont have this hour
                    //todo request all qsos
                    Seq(SendContainer(RequestQsosForHour(fdHour, ns.nodeAddress, nodeAddress, getClass), ns.nodeAddress))
                }
              }
              if (messages.nonEmpty) {
                logger.debug(s"Need ${messages.length} FdHours, will process up to 5 of them.")
                messages.take(5).foreach(httpClient ! _)
              }
            }
          }
        }
      }

    case Purge =>
      val deadNodes: List[NodeAddress] = nodeHistory.grimReaper()
      if (deadNodes.nonEmpty) {
        logger.whenDebugEnabled {
          val str = deadNodes.map(_.display).mkString(" ")
          logger.debug(s"DeadNodes: $str")
        }
        clusterTable.purge(deadNodes)
        fdHours.purge(deadNodes)
      }

    case requestUuidsForHour: RequestUuidsForHour =>
      httpClient ! requestUuidsForHour

    case uc: UuidsAtHost =>
      (store ? uc).mapTo[RequestQsosForUuids].map(requestQsosForUuids =>
        httpClient ! SendContainer(requestQsosForUuids, uc.nodeAddress)
      )

    case x =>
      logger.error(s"Unexpected message: $x")
  }

}

case object Purge

@Singleton
class ClusterSender @Inject()(implicit val injector: Injector) extends ActorInject with ActorSender {
  val actor: ActorRef = injectTopActor[ClusterActor]("cluster")
}


