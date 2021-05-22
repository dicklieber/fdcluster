
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

package org.wa9nnn.fdcluster.model.sync

import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.javafx.NamedCellProvider
import org.wa9nnn.fdcluster.model.{Station, Journal, NodeAddress, QsoMetadata}
import org.wa9nnn.fdcluster.store.network.FdHour

import java.time.Instant

/**
 *
 * @param nodeAddress      our IP and instance.
 * @param qsoCount         of QSOs in db.
 * @param qsoHourDigests   for quickly determining what we have.
 * @param qsoMetadata      band, mode, operator etc.
 * @param currentStation   band mode and current operator
 * @param stamp            when this message was generated.
 * @param ver                FDCLuster Version that built this so we can detect mismatched versions.
 *
 */
case class NodeStatus(nodeAddress: NodeAddress,
                      qsoCount: Int,
                      qsoHourDigests: List[QsoHourDigest],
                      qsoMetadata: QsoMetadata,
                      currentStation: Station,
                      contest: Contest,
                      journal: Journal,
                      stamp: Instant = Instant.now(),
                      ver: String = BuildInfo.canonicalVersion) extends NamedCellProvider[NodeStatus] with  ClusterMessage{


  assert(currentStation != null, "null BandModeOperator")

  def digestForHour(fdHour: FdHour): Option[QsoHourDigest] = {
    qsoHourDigests.find(_.fdHour == fdHour)
  }

  /**
   *
   * @return [[FdHour]]s in the node
   */
  def knownHours: Set[FdHour] = {
    qsoHourDigests.map(_.fdHour).toSet
  }

}


