package org.wa9nnn.fdcluster.model.sync

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{CurrentStation, NodeAddress, QsoMetadata}

import java.time.Instant

class NodeStatusQueueSpec extends Specification {
  val address0 = new NodeAddress()
  val ns0: NodeStatus = NodeStatus(nodeAddress = address0,
    qsoCount = 42,
    digest = "123445",
    qsoHourDigests = List.empty,
    bandModeOperator = CurrentStation(),
    qsoMetadata = QsoMetadata(),
    qsoRate = 1.0,
    contest = Contest()
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
