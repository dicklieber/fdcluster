
package org.wa9nnn.fdcluster.store.network.cluster

import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.FdHour

import scala.collection.concurrent.TrieMap
import scala.collection.immutable

/**
 * Mutable state of all nodes in the cluster, including this one.
 * From this nodes point-of-view.
 *
 * @param ourNodeAddress who we are.
 */
class ClusterState(ourNodeAddress: NodeAddress) extends LazyLogging {
  private val nodes: TrieMap[NodeAddress, NodeStateContainer] = TrieMap.empty

  def update(nodeStatus: NodeStatus): Unit = {
    val nodeAddress = nodeStatus.nodeAddress
    val nodeState = nodes.getOrElseUpdate(nodeAddress, new NodeStateContainer(nodeStatus, ourNodeAddress))
    nodeState.update(nodeStatus)
  }

  def dump: Iterable[NodeStateContainer] = {
    nodes.values
  }

  def knownHoursInCluster: List[FdHour] = {
    val setBuilder = Set.newBuilder[FdHour]
    for {
      nodeStateContainer <- nodes.values
      hourInNode <- nodeStateContainer.knownHours
    } {
      setBuilder += hourInNode
    }
    setBuilder.result().toList.sorted
  }

  def hoursToSync(): List[FdHour] = {
    def getForHour(fdHour: FdHour): (Option[NodeFdHourDigest], Seq[NodeFdHourDigest]) = {
      val allForHour: List[NodeFdHourDigest] =
        (for {
          nsc <- nodes.values
          maybe <- nsc.forHour(fdHour)
        } yield {
          maybe
        }).toList
      val ours = allForHour.find(_.nodeAddress == ourNodeAddress)
      val others: immutable.Seq[NodeFdHourDigest] = allForHour.filter(_.nodeAddress != ourNodeAddress)
      ours → others
    }


    val todoStuff = knownHoursInCluster.flatMap { fdHour ⇒

      //      val nodesToConsider: List[NodeFdHourDigest] = getForHour(fdHour)
      val (ours, others) = getForHour(fdHour)
      logger.trace(s"ours: $ours  others: $others")
      //todo  nodes with different digest that ours
      //todo  of those pick the one with the most
      List.empty
    }
    List.empty
  }

  def otherNodeWithMostThanUs(): Option[URL] = {
    val us: Option[NodeStatus] = nodes.get(ourNodeAddress).map(_.nodeStatus)
    us match {
      case Some(ourStatus: NodeStatus) ⇒
        val ourDigest = ourStatus.digest
        val ourCount = ourStatus.qsoCount
        nodes.values
          .filter(nsc ⇒ nsc.nodeAddress != ourNodeAddress && (nsc.qsoCount > ourCount | nsc.nodeStatus.digest != ourDigest))
          .toList
          .sortBy(_.qsoCount)
          .lastOption
          .map(_.url)
      case None ⇒
        None
    }
  }
}


