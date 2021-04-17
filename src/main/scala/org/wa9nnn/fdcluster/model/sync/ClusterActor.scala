package org.wa9nnn.fdcluster.model.sync

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import org.wa9nnn.fdcluster.http.{HttpClientActor, RequestQsosForUuids, Sendable}
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, UuidsAtHost}
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.cluster.{ClusterState, NodeStateContainer}
import org.wa9nnn.fdcluster.store.{DumpCluster, Sync}
import org.wa9nnn.util.StructuredLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class ClusterActor(nodeAddress: NodeAddress,
                   @Named("store") store: ActorRef,
                  ) extends Actor with StructuredLogging {
  private val clusterState = new ClusterState(nodeAddress)
  private implicit val timeout: Timeout = Timeout(5 seconds)

  private val httpClient: ActorRef = context.actorOf(Props(classOf[HttpClientActor], store, context.self))

  override def receive: Receive = {

    case s: Sendable => httpClient ! s

    /**
     * Start a sync operation
     */
    case Sync ⇒
      clusterState.otherNodeWithMostThanUs() match {
        case Some(bestNode: NodeStateContainer) ⇒
          bestNode.nodeStatus.qsoHourDigests
//            .take(1)
            .foreach(qsoHourDigest => {
            val msg = RequestUuidsForHour(List(qsoHourDigest.fdHour))
            val requestUuidsForHour = Sendable(msg,bestNode.nodeAddress.uri, context.self)
            whenTraceEnabled {
              requestUuidsForHour.toString
            }
            httpClient ! requestUuidsForHour
          }
          )

        //          val value = Sendable(RequestQsosForHours.apply(), bestNode, context.self)
        //          httpClient ! value
        case None ⇒
      }
    case ns: NodeStatus ⇒
      logger.trace(s"Got NodeStatus from ${ns.nodeAddress}")
      clusterState.update(ns)

    case DumpCluster ⇒
      sender ! clusterState.dump

    //      val requestUuidsForHour = RequestUuidsForHour(List(FdHour(11, 22)))
    //      cluster ! Sendable(requestUuidsForHour, nodeAddress.uri, store)
    //


    case rufh: RequestUuidsForHour =>
      httpClient ! rufh

    case uc: UuidsAtHost =>
      (store ? uc).mapTo[RequestQsosForUuids].map(requestQsosForUuids =>
        httpClient ! Sendable(requestQsosForUuids, uc.nodeAddress.uri, store)
      )

    case x =>
      logger.error(s"Unexpected message: $x")
  }
}
