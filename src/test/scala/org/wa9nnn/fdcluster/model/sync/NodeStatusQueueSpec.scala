package org.wa9nnn.fdcluster.model.sync

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{Station, Journal, NodeAddress, QsoMetadata}

import java.time.Instant

class NodeStatusQueueSpec extends Specification {
  val address0 = new NodeAddress()
  val ns0: NodeStatus = NodeStatus(nodeAddress = address0,
    qsoCount = 42,
    qsoHourDigests = List.empty,
    currentStation = Station(),
    qsoMetadata = QsoMetadata(),
    contest = Contest(),
    journal = Journal()
  )

  "NodeStatusQueue" >> {
    "Happy" >> {
      val queue = new NodeStatusQueue()
      queue.size must beEqualTo(0)
      queue.take() must beEmpty
      queue.add(ns0)
      queue.take() must beSome(ns0)
      queue.take() must beEmpty

    }
    "latest" >> {
      val queue = new NodeStatusQueue()
      queue.add(ns0)
      val nsLater: NodeStatus = ns0.copy(stamp = Instant.now())
      queue.size must beEqualTo(1)
      queue.add(nsLater)
      queue.size must beEqualTo(1)
      queue.take() must beSome(nsLater)
      queue.take() must beEmpty
      queue.size must beEqualTo(0)
    }
  }
}
