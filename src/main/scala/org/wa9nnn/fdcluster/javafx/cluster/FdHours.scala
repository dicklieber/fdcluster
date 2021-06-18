package org.wa9nnn.fdcluster.javafx.cluster

import akka.http.scaladsl.model.headers.Age
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

  def colorHours(fdHour: FdHour): Unit = {
    HourColorer(data.cellsForRow(fdHour))
  }

  /**
   *
   * @param nodeStatus incoming.
   */
  def update(nodeStatus: NodeStatus): Unit = {
    val nodeAddress = nodeStatus.nodeAddress

    val startMatrixSize = data.size

    metadataMap.getOrElseUpdate(nodeAddress, NodeMetadata(nodeAddress)).update(nodeStatus)
    nodeStatus.qsoHourDigests.foreach { qhd =>
      val cell: QsoDigestPropertyCell = data.getOrElseUpdate(qhd.fdHour, nodeAddress, {
        qhd.PropertyCell
      })
      cell.update(NamedValue(qhd.fdHour, qhd))
      colorHours(qhd.fdHour)
    }
    if (startMatrixSize != data.size) {
      layoutVersion.value = layoutVersion.value + 1
    }

  }

  def purge(deadNodes: List[NodeAddress]): Unit = {
    deadNodes.foreach { node =>
      metadataMap.remove(node)
      data.removeColumn(node)
    }
    layoutVersion.value = layoutVersion.value + 1
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

case class NodeMetadata(nodeAddress: NodeAddress) {

  val ageCell: PropertyCellAge = PropertyCellFactory(ValueName.Age, Instant.now()).asInstanceOf[PropertyCellAge]
  val qslCountCell: PropertyCell = PropertyCellFactory(ValueName.QsoCount, 0)

  def update(nodeStatus: NodeStatus): Unit = {
    ageCell.update(NamedValue(ValueName.Age, nodeStatus.stamp))
    qslCountCell.update(NamedValue(ValueName.QsoCount, nodeStatus.qsoCount))
  }

  def clear(): Unit = {
    ageCell.clear()
  }
}

case class Key[R <: Ordered[R], C <: Ordered[C]](row: R, column: C)

