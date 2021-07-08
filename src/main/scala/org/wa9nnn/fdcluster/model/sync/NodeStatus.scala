
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
import org.wa9nnn.fdcluster.http.DestinationActor
import org.wa9nnn.fdcluster.javafx.cluster.{NamedValue, NamedValueCollector, ValueName}
import org.wa9nnn.fdcluster.javafx.sync.ResponseMessage
import org.wa9nnn.fdcluster.model.MessageFormats.Digest
import org.wa9nnn.fdcluster.model.sync.NodeStatus.serialNumbers
import org.wa9nnn.fdcluster.model.{Journal, NodeAddress, Station}
import org.wa9nnn.fdcluster.store.JsonContainer
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.webclient.Session
import org.wa9nnn.fdcluster.model.MessageFormats._
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @param nodeAddress        our IP and instance.
 * @param qsoCount           of QSOs in db.
 * @param qsoHourDigests     for quickly determining what we have.
 * @param station            band mode and current operator
 * @param ver                FdCluster version that built this so we can detect mismatched versions.
 * @param sn                 so something changes
 *
 */
case class BaseNodeStatus(nodeAddress: NodeAddress,
                          qsoCount: Int = 0,
                          qsoHourDigests: List[QsoHourDigest] = List.empty,
                          station: Station = Station(),
                          contest: Option[Contest] = None,
                          journal: Option[Journal] = None,
                          sessions: List[Session] = List.empty,
                          osName: String = s"${System.getProperty("os.name")} ${System.getProperty("os.version")}",
                          ver: String = BuildInfo.version,
                          sn: Int = serialNumbers.getAndIncrement()) extends ClusterMessage {

  assert(station != null, "null BandModeOperator")


  def collectValues(collector: NamedValueCollector): Iterable[NamedValue] = {
    import ValueName._

    nodeAddress.collectNamedValues(collector)
    collector(QsoCount, qsoCount)
    collector(FdHours, qsoHourDigests.length)
    station.collectNamedValues(collector)
    contest.foreach { contest =>
      collector(CallSign, contest.callSign)
      collector(ValueName.Contest, contest.id)
    }
    collector(ValueName.Journal, journal.map(_.journalFileName).getOrElse("Not Set"))
    collector(Version, ver)
    collector(OS, osName)
    collector(Sessions, sessions.map(_.station.operator).mkString("\n"))
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


case class NodeStatus(nodeStatus: BaseNodeStatus, digest: Digest, stamp: Instant = Instant.now()) extends ClusterMessage with ResponseMessage {
  def digestForHour(fdHour: FdHour) = DestinationActor.cluster

  def qsoHourDigests: List[QsoHourDigest] = nodeStatus.qsoHourDigests

  def contest: Option[Contest] = nodeStatus.contest

  def journal: Option[Journal] = nodeStatus.journal

  val nodeAddress: NodeAddress = nodeStatus.nodeAddress

  lazy val values: Iterable[NamedValue] = {
    import ValueName._
    val collector = new NamedValueCollector()
    nodeStatus.collectValues(collector)
    collector(Age, stamp)
    collector(Digest, digest)
    collector.result
  }

  lazy val heartBeatMessage: JsonContainer = {
   JsonContainer( HeartBeatMessage(nodeStatus.nodeAddress, digest, stamp))
  }
  override val destination: DestinationActor = DestinationActor.cluster
}

object NodeStatus {
  def apply(nodeStatus: BaseNodeStatus): NodeStatus = {
    nodeStatus.toString
    val sha256 = MessageDigest.getInstance("SHA-256")
    val digest: Array[Byte] = sha256.digest(nodeStatus.toString.getBytes)

    val encoder = java.util.Base64.getEncoder
    val bytes1 = encoder.encode(digest)
    val sDigest = new String(bytes1)

    new NodeStatus(nodeStatus, sDigest)
  }

  val serialNumbers: AtomicInteger = new AtomicInteger()

}

case class HeartBeatMessage(nodeAddress: NodeAddress, nodStatusDigest: Digest, stamp: Instant = Instant.now) extends ClusterMessage {
  def needNodeStatus(candidate: Option[HeartBeatMessage]): Boolean = {
    candidate.forall(_.nodStatusDigest != nodStatusDigest)
  }
}


