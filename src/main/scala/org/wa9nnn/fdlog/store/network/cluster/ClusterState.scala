
package org.wa9nnn.fdlog.store.network.cluster

import com.typesafe.scalalogging.LazyLogging
import jdk.jshell.spi.ExecutionControl.NotImplementedException
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdlog.store.network.FdHour

import scala.collection.concurrent.TrieMap

class ClusterState(ourNodeAddress: NodeAddress) extends LazyLogging{
  private val nodes: TrieMap[NodeAddress, NodeStateContainer] = TrieMap.empty

  def update(nodeStatus: NodeStatus): Unit = {
    val nodeAddress = nodeStatus.nodeAddress
    val nodeState = nodes.getOrElseUpdate(nodeAddress, new NodeStateContainer(nodeStatus))
    nodeState.update(nodeStatus)
  }

  def dump: Iterable[NodeStateContainer] = {
    nodes.values
  }

  def knownHoursInCluster: List[FdHour] = {
    val setBuilder = Set.newBuilder[FdHour]
    for {
      nodeStateContainer <- nodes.values
      hourInNode <- nodeStateContainer.knownHours
    } {
      setBuilder += hourInNode
    }
    setBuilder.result().toList.sorted
  }

  def hoursToSync(): List[FdHour] = {
    def getForHour(fdHour: FdHour): (NodeFdHourDigest, List[NodeFdHourDigest]) = {
      val allForHour: List[NodeFdHourDigest] =
        (for {
          nsc <- nodes.values
          maybe <- nsc.forHour(fdHour)
        } yield {
          maybe
        }).toList
      val ours = allForHour.find(_.nodeAddress == ourNodeAddress).head
      val others = allForHour.filter(_.nodeAddress != ourNodeAddress)
      ours → others
    }


   val todoStuff =  knownHoursInCluster.flatMap { fdHour ⇒

      //      val nodesToConsider: List[NodeFdHourDigest] = getForHour(fdHour)
      val (ours, others) = getForHour(fdHour)
      logger.debug(s"ours: $ours  others: $others")
      //todo  nodes with different digest that ours
      //todo  of those pick the one with the most
     List.empty
    }
    List.empty
  }

}

