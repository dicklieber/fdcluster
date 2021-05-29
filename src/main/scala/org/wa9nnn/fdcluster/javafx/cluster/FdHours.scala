package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.contest.JournalProperty
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoDigestPropertyCell}
import org.wa9nnn.fdcluster.store.network.FdHour
import scalafx.beans.property.IntegerProperty

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

/**
 * Holds a cluster status for FdHours
 */
@Singleton
class FdHours @Inject()(journalProperty: JournalProperty) extends LazyLogging {
  private val data: Matrix[FdHour, NodeAddress, QsoDigestPropertyCell] = new Matrix[FdHour, NodeAddress, QsoDigestPropertyCell]
  private val metadataMap: TrieMap[NodeAddress, NodeMetadata] = new TrieMap[NodeAddress, NodeMetadata]()

  val layoutVersion: IntegerProperty = new IntegerProperty

  journalProperty.journalFilePathProperty.onChange { (_, _, _) =>
    data.clear()
    metadataMap.values.foreach(_.clear())
    metadataMap.clear()
    layoutVersion.value = layoutVersion.value + 1
  }

  def colorHours(fdHour: FdHour): Unit ={
    HourColorer(data.cellForRow(fdHour))
  }

  /**
   *
   * @param nodeStatus incoming.
   * @return true if we added one or more FdHour row, indicating that the gridPane needs to be refreshed.
   */
  def update(nodeStatus: NodeStatus): Unit = {
    val nodeAddress = nodeStatus.nodeAddress

    val startMatrixSize = data.size

    metadataMap.getOrElseUpdate(nodeAddress, NodeMetadata(nodeAddress)).update(nodeStatus)
    nodeStatus.qsoHourDigests.foreach { qhd =>
      val cell: QsoDigestPropertyCell = data.getOrElseUpdate(Key(qhd.fdHour, nodeAddress), {
        qhd.PropertyCell
      })
      cell.update(qhd)
      colorHours(qhd.fdHour)
    }
    if (startMatrixSize != data.size) {
      layoutVersion.value = layoutVersion.value + 1
    }

  }

  def clear(): Unit = {
    data.clear()
  }

  def rows: List[FdHour] = data.rows

  /**
   * iCol in  [[ColInfo]] might not be consistent if [[update()]] returned true.
   *
   * @return
   */
  def metadataColumns: List[ColInfo] =
    metadataMap.
      toList.sortBy(_._1)
      .zipWithIndex
      .map { case ((na: NodeAddress, nodeMetadata: NodeMetadata), iCol) =>
        ColInfo(na, nodeMetadata, iCol)
      }

  def get(fdHour: FdHour, nodeAddress: NodeAddress): Option[QsoDigestPropertyCell] = {
    data.get(fdHour, nodeAddress)
  }
}

case class ColInfo(nodeAddress: NodeAddress, nodeMetadata: NodeMetadata, iCol: Int)

case class NodeMetadata(nodeAddress: NodeAddress,
                        ageCell: SimplePropertyCell = SimplePropertyCell(),
                        qsoCountCell: SimplePropertyCell = SimplePropertyCell.css("number", "clusterCell")) {
  def update(nodeStatus: NodeStatus): Unit = {
    ageCell.update(nodeStatus.stamp)
    qsoCountCell.update(nodeStatus.qsoCount)
  }

  def clear(): Unit = {
    ageCell.clear()
  }
}

object NodeMetadata {
  def apply(nodeAddress: NodeAddress, stamp: Instant): NodeMetadata = {
    new NodeMetadata(nodeAddress, SimplePropertyCell(nodeAddress, stamp))
  }
}

case class Key[R <: Ordered[R], C <: Ordered[C]](row: R, column: C)

