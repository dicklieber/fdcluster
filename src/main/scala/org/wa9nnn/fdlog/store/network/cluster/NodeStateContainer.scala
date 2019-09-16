
package org.wa9nnn.fdlog.store.network.cluster

import java.time.LocalDateTime

import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.model.sync.NodeStatus
import org.wa9nnn.fdlog.store.network.FdHour


class NodeStateContainer(initialNodeStatus: NodeStatus) {
  def digestForHour(fdHour: FdHour): String  = {
    nodeStatus.digestForHour(fdHour)
  }


  def qsoCount: Int = nodeStatus.count

  val firstContact: LocalDateTime = LocalDateTime.now()
  var nodeStatus: NodeStatus = initialNodeStatus

  def nodeAddress: NodeAddress = nodeStatus.nodeAddress


  def update(nodeStatus: NodeStatus): Unit = {
    this.nodeStatus = nodeStatus
  }


}
