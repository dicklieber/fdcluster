
package org.wa9nnn.fdlog.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.store.network.FdHour
import org.wa9nnn.fdlog.store.network.cluster.NodeStateContainer
import scalafx.beans.property.{ObjectProperty, ReadOnlyStringWrapper}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class ClusterTable extends LazyLogging {
  private val data = ObservableBuffer[Row](Seq.empty)
  val tableView = new TableView[Row](data)

  def refresh(nodes: Iterable[NodeStateContainer]): Unit = {
    implicit val byAddress = new TrieMap[NodeAddress, NodeStateContainer]()
    val hours: mutable.Set[FdHour] = mutable.Set.empty
    nodes.foreach { nodeStateContainer ⇒
      byAddress.put(nodeStateContainer.nodeAddress, nodeStateContainer)
      nodeStateContainer.nodeStatus.qsoHourDigests.foreach(qsoHourDigest ⇒
        hours add qsoHourDigest.startOfHour
      )
    }

    val orderedNodes = byAddress.keySet.toList.sorted

    /**
     *
     * @param rowHeader string for 1st column
     * @param callback  how to extract body cell from a NodeStateContainer. Will be called for each [[NodeStateContainer]]
     * @return a row for the table
     */
    def buildRow(rowHeader: String, callback: NodeStateContainer ⇒ Any): Row = {
      Row(rowHeader, orderedNodes.map(nodeAddress ⇒ {
        val maybeContainer = byAddress.get(nodeAddress)
        callback(maybeContainer.get)
      }))
    }

    def buildHours: List[Row] = {
      hours.toList.sorted.map { fdHour ⇒
        Row(fdHour.toString,
          orderedNodes.map {
            byAddress(_).digestForHour(fdHour).getOrElse("--")
          }
        )
      }
    }

    val rows: List[Row] = List(
      buildRow("Started", _.firstContact),
      buildRow("Last", _.nodeStatus.stamp),
      buildRow("QSOs", _.nodeStatus.count),
      buildRow("Band", _.nodeStatus.currentStation.bandMode.band.band),
      buildRow("Mode", _.nodeStatus.currentStation.bandMode.mode.name()),
      buildRow("Operator", _.nodeStatus.currentStation.ourStation.operator),
      buildRow("Rig", _.nodeStatus.currentStation.ourStation.rig),
      buildRow("Antenna", _.nodeStatus.currentStation.ourStation.antenna),
    ) ++ buildHours

    data.clear()
    data.addAll(rows: _*)


    def buildColumns = {
      val colTexts: List[String] = orderedNodes.map(_.display)

      colTexts.zipWithIndex.map(e ⇒
        new TableColumn[Row, Any] {
          sortable = false
          val col = e._2
          text = e._1
          cellValueFactory = { x ⇒
            val r = x.value.cells(col)
            new ObjectProperty(x.value, "row", r)
          }

          cellFactory = { _ =>
            new FdClusterTableCell[Row, Any]
          }
        }
      )
    }

    val rowHeaderCol = new TableColumn[Row, String] {
      text = "Node"
      cellValueFactory = { q ⇒
        ReadOnlyStringWrapper(q.value.rowHeader)
      }
      sortable = false
    }

    tableView.columns.clear()
    tableView.columns += rowHeaderCol
    buildColumns.foreach(tc ⇒
      tableView.columns += tc
    )
  }
}

/**
 *
 * @param rowHeader name show in 1st column of row.
 * @param cells     things that an be rendered.
 */
case class Row(rowHeader: String, cells: Seq[Any])

