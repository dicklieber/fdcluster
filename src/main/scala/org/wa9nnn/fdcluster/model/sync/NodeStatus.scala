
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
import org.wa9nnn.fdcluster.javafx.cluster.{NamedValue, NamedValueCollector, ValueName}
import org.wa9nnn.fdcluster.model.{Journal, NodeAddress, Station}
import org.wa9nnn.fdcluster.store.network.FdHour

import java.time.Instant

/**
 *
 * @param nodeAddress        our IP and instance.
 * @param qsoCount           of QSOs in db.
 * @param qsoHourDigests     for quickly determining what we have.
 * @param station            band mode and current operator
 * @param stamp              when this message was generated.
 * @param ver                FDCLuster Version that built this so we can detect mismatched versions.
 *
 */
case class NodeStatus(nodeAddress: NodeAddress,
                      qsoCount: Int = 0,
                      qsoHourDigests: List[QsoHourDigest] = List.empty,
                      station: Station = Station(),
                      contest: Option[Contest] = None,
                      journal: Option[Journal] = None,
                      osName: String = s"${System.getProperty("os.name")} ${System.getProperty("os.version")}",
                      stamp: Instant = Instant.now(),
                      ver: String = BuildInfo.version) extends ClusterMessage {

  assert(station != null, "null BandModeOperator")

  def values: Iterable[NamedValue] = {
    import ValueName._
    val collector = new NamedValueCollector()
    nodeAddress.collectNamedValues(collector)
    collector(QsoCount, qsoCount)
    collector(FdHours, qsoHourDigests.length)
    station.collectNamedValues(collector)
    contest.foreach { contest =>
      collector(CallSign, contest.callSign)
      collector(ValueName.Contest, contest.id)
    }
    collector(ValueName.Journal, journal.map(_.journalFileName).getOrElse("Not Set"))
    collector(Stamp, stamp)
    collector(Version, ver)
    collector(OS, osName)
    qsoHourDigests.foreach { qsd =>
      collector(qsd.fdHour, qsd)
    }
    collector.result
  }

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


