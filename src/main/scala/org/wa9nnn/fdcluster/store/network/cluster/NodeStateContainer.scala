
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

import java.net.URL
import java.time.LocalDateTime
import org.wa9nnn.fdcluster.javafx.cluster.StyledAny
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdcluster.store.network.{FdHour, cluster}

/**
 *
 * @param initialNodeStatus don't allow an empty container.
 * @param ourNodeAddress    our address
 */
class NodeStateContainer(initialNodeStatus: NodeStatus, ourNodeAddress: NodeAddress) {
   val isUs: Boolean = initialNodeStatus.nodeAddress == ourNodeAddress

  def styleForUs(sa: StyledAny): StyledAny = {
      sa.withCssClass(cssStyles)
  }
  lazy val cssStyles: Seq[String] =   {
    if (isUs) {
      Seq("ourNode")
    } else {
      Seq.empty
    }
  }


  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    nodeStatus.digestForHour(fdHour)
  }

  def qsoCount: Int = nodeStatus.qsoCount

  var nodeStatus: NodeStatus = initialNodeStatus

  def nodeAddress: NodeAddress = nodeStatus.nodeAddress

  def update(nodeStatus: NodeStatus): Unit = {
    this.nodeStatus = nodeStatus
  }

  def uri: Uri = {
    nodeStatus.nodeAddress.uri
  }


  def forHour(fdHour: FdHour): Option[NodeFdHourDigest] = {
    nodeStatus
      .qsoHourDigests
      .find(_.fdHour == fdHour)
      .map(qhd ⇒ cluster.NodeFdHourDigest(nodeAddress, qhd))
  }

  /**
   *
   * @return [[FdHour]]s in the node
   */
  def knownHours: Set[FdHour] = {
    nodeStatus.qsoHourDigests.map(_.fdHour).toSet
  }


}

case class NodeFdHourDigest(nodeAddress: NodeAddress, qsoHourDigest: QsoHourDigest)
