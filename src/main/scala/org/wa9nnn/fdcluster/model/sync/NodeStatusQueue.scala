package org.wa9nnn.fdcluster.model.sync

import org.wa9nnn.fdcluster.model.NodeAddress

import scala.collection.concurrent.TrieMap

/**
 * A queue of [[NodeStatus]], one for each [[NodeAddress]]
 * A newer NodeStatus replaces an existing one.
 */
class NodeStatusQueue {
  private val map: TrieMap[NodeAddress, NodeStatus] = new TrieMap[NodeAddress, NodeStatus]()

  /**
   *
   * @return the oldest [[NodeStatus]] among the nodes in the queue.
   */
  def take(): Option[NodeStatus] = {
    map.
      values
      .toSeq
      .sortBy(_.stamp)
      .reverse
      .headOption
      .flatMap { ns: NodeStatus =>
        map.remove(ns.nodeAddress)
      }
  }

  def add(incoming: NodeStatus): Unit = {
    map.put(incoming.nodeAddress, incoming)
  }

  def size: Int = map.size
}
