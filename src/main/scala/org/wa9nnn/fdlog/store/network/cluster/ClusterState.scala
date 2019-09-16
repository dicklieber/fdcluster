
package org.wa9nnn.fdlog.store.network.cluster

import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.model.sync.NodeStatus

import scala.collection.concurrent.TrieMap

class ClusterState {

  private val nodes:TrieMap[NodeAddress, NodeStateContainer] = TrieMap.empty

  def update(nodeStatus: NodeStatus): Unit = {
    val nodeAddress = nodeStatus.nodeAddress
    val nodeState = nodes.getOrElseUpdate(nodeAddress, new NodeStateContainer(nodeStatus))
    nodeState.update(nodeStatus)
  }

  def dump: Iterable[NodeStateContainer] = {
    nodes.values
  }
}
