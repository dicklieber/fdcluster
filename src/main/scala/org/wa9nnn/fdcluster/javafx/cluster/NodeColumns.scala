package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

/**
 * Hold the [[PropertyCell]]s for a node.
 * Values for the cells can be updated when a new [[org.wa9nnn.fdcluster.model.sync.NodeStatus]] is received.
 *
 */
@Singleton
class NodeColumns @Inject()(fdHours: FdHours, ourNodeAddress: NodeAddress) {
  def nodeCells: Seq[NodeCells] = {
    map
      .values
      .toSeq
      .sortBy(_.nodeAddress)
  }

  val map = new TrieMap[NodeAddress, NodeCells]()

  /**
   *
   * @param nodeStatus incoming.
   * @return true if we added one or more NodeCells columns, indicating that the gridPane needs to laid out again.
   */
  def update(nodeStatus: NodeStatus): Boolean = {
    var changedLayout = false
    fdHours.update(nodeStatus)

    val r: NodeCells = map.getOrElseUpdate(nodeStatus.nodeAddress, {
      changedLayout = true
      NodeCells(nodeStatus.nodeAddress, fdHours, ourNodeAddress)

    })

    r.update(nodeStatus)
    changedLayout
  }
}