
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

package org.wa9nnn.fdcluster.store.network.cluster

import akka.http.scaladsl.model.Uri
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.util.StructuredLogging

import java.net.URL
import scala.collection.concurrent.TrieMap
import scala.collection.immutable

/**
 * Mutable state of all nodes in the cluster, including this one.
 * From this nodes point-of-view.
 *
 * @param ourNodeAddress who we are.
 */
class ClusterState(ourNodeAddress: NodeAddress) extends StructuredLogging with DefaultInstrumented {
  private val nodes: TrieMap[NodeAddress, NodeStateContainer] = TrieMap.empty
  metrics.gauge("node count"){
    nodes.size
  }

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

  def otherNodeWithMostThanUs(): Option[NodeStateContainer] = {
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
      case None ⇒
        None
    }
  }
}


