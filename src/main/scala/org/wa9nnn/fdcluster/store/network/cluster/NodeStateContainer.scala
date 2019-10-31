
package org.wa9nnn.fdcluster.store.network.cluster

import java.net.URL
import java.time.LocalDateTime

import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdcluster.store.network.{FdHour, cluster}

class NodeStateContainer(initialNodeStatus: NodeStatus) {

  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    nodeStatus.digestForHour(fdHour)
  }

  def qsoCount: Int = nodeStatus.qsoCount

  val firstContact: LocalDateTime = LocalDateTime.now()
  var nodeStatus: NodeStatus = initialNodeStatus

  def nodeAddress: NodeAddress = nodeStatus.nodeAddress

  def update(nodeStatus: NodeStatus): Unit = {
    this.nodeStatus = nodeStatus
  }

  def url:URL = {
    nodeStatus.apiUrl
  }

  def forHour(fdHour: FdHour): Option[NodeFdHourDigest] = {
    nodeStatus
      .qsoHourDigests
      .find(_.startOfHour == fdHour)
      .map(qhd ⇒ cluster.NodeFdHourDigest(nodeAddress, qhd))
  }

  /**
   *
   * @return [[FdHour]]s in the node
   */
  def knownHours: Set[FdHour] = {
    nodeStatus.qsoHourDigests.map(_.startOfHour).toSet
  }


}

case class NodeFdHourDigest(nodeAddress: NodeAddress, qsoHourDigest: QsoHourDigest)
