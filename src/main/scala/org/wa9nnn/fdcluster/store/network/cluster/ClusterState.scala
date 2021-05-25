
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

import com.github.andyglow.config._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.javafx.cluster.ClusterTable
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState.NodeStatusProperty
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableMap

import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}

/**
 * Mutable state of all nodes in the cluster, including this one.
 * From this nodes point-of-view.
 *
 * @param ourNodeAddress who we are.
 */
@Singleton
class ClusterState @Inject()(ourNodeAddress: NodeAddress, clusterTable: ClusterTable, config: Config) extends LazyLogging with DefaultInstrumented {

  private val nodeStatusLife: Duration = config.get[Duration]("fdcluster.cluster.nodeStatusLife")

  val nodes: ObservableMap[NodeAddress, NodeStatusProperty] = ObservableMap[NodeAddress, NodeStatusProperty]()

  def size: Int = nodes.size

  metrics.gauge[Int]("Cluster Size") {
    size
  }


  def update(nodeStatus: NodeStatus): Unit = {
    clusterTable.update( nodeStatus.values)
  }

  def purge(): Unit = {
    val tooOldStamp = Instant.now().minus(nodeStatusLife)
    nodes.values
      .map(_.value)
      .filter(_.stamp.isBefore(tooOldStamp))
      .foreach(ns => {
        nodes.remove(ns.nodeAddress)
      })
  }

  def dump: Iterable[NodeStatus] = {
    nodes.values.map(_.value)
  }

  def knownHoursInCluster: List[FdHour] = {
    val setBuilder = Set.newBuilder[FdHour]
    for {
      nodeStateProperty <- nodes.values
      hourInNode <- nodeStateProperty.value.knownHours
    } {
      setBuilder += hourInNode
    }
    setBuilder.result().toList.sorted
  }

}

object ClusterState {
  type NodeStatusProperty = ObjectProperty[NodeStatus]
}


