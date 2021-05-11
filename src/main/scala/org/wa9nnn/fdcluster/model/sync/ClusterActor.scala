package org.wa9nnn.fdcluster.model.sync

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import org.wa9nnn.fdcluster.http.HttpClientActor
import org.wa9nnn.fdcluster.javafx.sync._
import org.wa9nnn.fdcluster.model.{ContestProperty, CurrentStation, NodeAddress, QsoMetadata}
import org.wa9nnn.fdcluster.store.DumpCluster
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState
import org.wa9nnn.util.StructuredLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class ClusterActor(nodeAddress: NodeAddress,
                   @Named("store") store: ActorRef,
                   @Named("nodeStatusQueue") nodeStatusQueue: ActorRef,
                   clusterState: ClusterState,
                   contestProperty: ContestProperty,
                  ) extends Actor with StructuredLogging {
  private implicit val timeout: Timeout = Timeout(5 seconds)
  context.system.scheduler.scheduleAtFixedRate(2 seconds, 17 seconds, self, Purge)

  private val httpClient: ActorRef = context.actorOf(Props(classOf[HttpClientActor], store, context.self))

  private var ourNodeStatus: NodeStatus = NodeStatus(
    nodeAddress = nodeAddress,
    qsoCount = 0,
    qsoHourDigests = List.empty,
    qsoMetadata = QsoMetadata(),
    currentStation = CurrentStation(),
    contest = contestProperty.value)

  override def receive: Receive = {

    case s: SendContainer => httpClient ! s

    case ns: NodeStatus ⇒
      logger.trace(s"Got NodeStatus from ${ns.nodeAddress}")
      clusterState.update(ns)
      nodeStatusQueue ! ns
      contestProperty.saveIfNewer(ns.contest)

      (nodeStatusQueue ? NextNodeStatus).mapTo[Option[NodeStatus]].map {
        {
          _.map { ns =>
            if (ns.nodeAddress == nodeAddress) {
              ourNodeStatus = ns
            } else {
              val messages = ns.qsoHourDigests.flatMap { otherQsoHourDigest: QsoHourDigest =>
                val fdHour = otherQsoHourDigest.fdHour
                ourNodeStatus.digestForHour(fdHour) match {
                  case Some(ourQsoHourDigest: QsoHourDigest) =>
                    if (ourQsoHourDigest.digest == otherQsoHourDigest.digest) {
                      // we match them, nothing to do
                      whenTraceEnabled(() => s"$fdHour matches")
                      Seq.empty

                    } else {
                      whenTraceEnabled(() => s"$fdHour unmatched digest starting uuid process to $ns.")
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
      clusterState.purge()
    case DumpCluster ⇒
      sender ! clusterState.dump

    case done: Done =>
    //todo keeps track of entire transaction

    case rufh: RequestUuidsForHour =>
      httpClient ! rufh

    case uc: UuidsAtHost =>
      (store ? uc).mapTo[RequestQsosForUuids].map(requestQsosForUuids =>
        httpClient ! SendContainer(requestQsosForUuids, uc.nodeAddress)
      )

    case x =>
      logger.error(s"Unexpected message: $x")
  }
}

case object Purge