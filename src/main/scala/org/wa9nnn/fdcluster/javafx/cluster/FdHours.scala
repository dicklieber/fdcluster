package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHourDigest}
import org.wa9nnn.fdcluster.store.network.FdHour

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
@Singleton
class FdHours @Inject() () extends LazyLogging{

  private val rows = new TrieMap[FdHour, Row]()

  def knownHours:List[FdHour] = {
    rows.keys.toList.sorted
  }

  def rowNames:List[PropertyCellName] = {
    ValueName.values.toList ++ knownHours
  }

  /**
   *
   * @param nodeStatus
   * @return true if we added one or more FdHour row, indicating that the gridPane needs to laid out again.
   */
  def update(nodeStatus: NodeStatus): Boolean = {
    var addedFdHours = Seq.empty[FdHour]
    nodeStatus.qsoHourDigests.foreach{qhd =>
      val fdHour = qhd.fdHour
      rows.getOrElseUpdate(fdHour, {
        addedFdHours = addedFdHours :+ fdHour
        Row(fdHour)
      }).update(nodeStatus.nodeAddress, qhd)
    }
    if (addedFdHours.nonEmpty) {
      logger.whenDebugEnabled {
        logger.debug(s"Discovered new FdHours: ${addedFdHours.map(_.display).mkString(", ")}")
      }
    }
    addedFdHours.nonEmpty
  }

  def clear(): Unit = {
    rows.clear()
  }
}

 case class Row(fdHour: FdHour) {
   protected val map = new TrieMap[NodeAddress, QsoHourDigest]()

   def allMatching:Boolean = {
     val values = map.values
     values.tail.forall(_ == values.head)
   }

   def update(nodeAddress: NodeAddress, qsoHourDigest: QsoHourDigest):Unit = {
     map.put(nodeAddress, qsoHourDigest)
   }
   def purge(nodeAddress: NodeAddress):Unit = {
     map.remove(nodeAddress)
   }
 }