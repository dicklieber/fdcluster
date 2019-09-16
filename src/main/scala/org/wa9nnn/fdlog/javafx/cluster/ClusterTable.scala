
package org.wa9nnn.fdlog.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.store.network.FdHour
import org.wa9nnn.fdlog.store.network.cluster.NodeStateContainer
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class ClusterTable extends LazyLogging {
  private val data = ObservableBuffer[Row](Seq.empty)
  val tableView = new TableView[Row](data)

  def refresh(nodes: Iterable[NodeStateContainer]):Unit= {
    implicit val byAddress = new TrieMap[NodeAddress, NodeStateContainer]()
    val hours: mutable.Set[FdHour] = mutable.Set.empty
    nodes.foreach { nodeStateContainer ⇒
      byAddress.put(nodeStateContainer.nodeAddress, nodeStateContainer)
      nodeStateContainer.nodeStatus.qsoHourDigests.foreach(qsoHourDigest ⇒
        hours add qsoHourDigest.startOfHour
      )
    }

    val orderedNodes = byAddress.keySet.toList.sorted

    def buildHeaderRow(orderedNodes: List[NodeAddress]): Row = {
      Row("", orderedNodes.map(_.display))
    }

    /**
     *
     * @param rowHeader string for 1st column
     * @param callback  how to extract body cell from a NodeStateContainer. Will be called for each [[NodeStateContainer]]
     * @return a row for the table
     */
    def buildRow(rowHeader: String, callback: NodeStateContainer ⇒ String): Row = {
      Row(rowHeader, orderedNodes.map(nodeAddress ⇒ {
        val maybeContainer = byAddress.get(nodeAddress)
        callback(maybeContainer.get)
      }))
    }

    def buildHours: List[Row] = {
      hours.toList.sorted.map { fdHour ⇒
        Row(fdHour.toString,
          orderedNodes.map {
            byAddress(_).digestForHour(fdHour)
          }
        )
      }
    }

    val rows: List[Row] = List(
//      buildHeaderRow(orderedNodes),
      buildRow("Started", _.firstContact.toString),
      buildRow("Last", _.nodeStatus.stamp.toString),
      buildRow("QSOs", _.nodeStatus.count.toString),
      buildRow("Band", _.nodeStatus.currentStation.bandMode.band.band),
      buildRow("Mode", _.nodeStatus.currentStation.bandMode.mode.name()),
      buildRow("Operator", _.nodeStatus.currentStation.ourStation.operator),
      buildRow("Rig", _.nodeStatus.currentStation.ourStation.rig),
      buildRow("Antenna", _.nodeStatus.currentStation.ourStation.antenna),
    ) ++ buildHours

    data.clear()
    data.addAll(rows:_*)

    def buildColumns: List[TableColumn[Row, String]] = {
      val colTexts: List[String] =  orderedNodes.map(_.display)

      colTexts.zipWithIndex.map(e ⇒
        new TableColumn[Row, String] {
          val col = e._2
          text = e._1
          cellValueFactory = { q =>
            logger.debug(s"cellValueFactory q: $q")

            val s = {
              try {
                q.value.cells(col)
              } catch {
                case x: Throwable ⇒ ""
              }
            }
            val wrapper = ReadOnlyStringWrapper(s)
            wrapper
          }
          prefWidth = 150
        }
      )
    }

    val rowHeaderCol = new TableColumn[Row, String]{
      text = ""
      cellValueFactory = {q ⇒
        ReadOnlyStringWrapper(q.value.rowHeader)
      }
    }

    tableView.columns.clear()
    tableView.columns += rowHeaderCol
    buildColumns.foreach(tc ⇒
      tableView.columns += tc
    )
  }
}

case class Row(rowHeader: String, cells: Seq[String])