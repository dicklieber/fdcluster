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

package org.wa9nnn.fdcluster.javafx.cluster

import _root_.scalafx.beans.property.ObjectProperty
import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.scene.control.{TableColumn, TableView}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.store.network.cluster.NodeStateContainer

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
 * Produce a [[TableView]]  of a collection of [[NodeStateContainer]]s.
 */
class ClusterTable extends LazyLogging {
  private val data: ObservableBuffer[Row] = ObservableBuffer.from(Seq.empty[Row])
  val tableView = new TableView[Row](data)

  def refresh(nodes: Iterable[NodeStateContainer]): Unit = {
    implicit val byAddress: mutable.Map[NodeAddress, NodeStateContainer] = new TrieMap[NodeAddress, NodeStateContainer]()
    val hours: mutable.Set[FdHour] = mutable.Set.empty
    nodes.foreach { nodeStateContainer ⇒
      byAddress.put(nodeStateContainer.nodeAddress, nodeStateContainer)
      nodeStateContainer.nodeStatus.qsoHourDigests.foreach(qsoHourDigest ⇒
        hours add qsoHourDigest.fdHour
      )
    }

    val orderedNodes: List[NodeAddress] = byAddress.keySet.toList.sorted

    //    /**
    //     *
    //     * @param rowHeader string for 1st column
    //     * @param callback  how to extract body cell from a NodeStateContainer. Will be called for each NodeStateContainer.
    //     * @return a row for the table
    //     */

    /**
     *
     * @param rowHeader
     * @param callback
     * @return
     */
    def buildRow(rowHeader: String, callback: NodeStateContainer ⇒ Any): Row = {
      MetadataRow(Cell(rowHeader), orderedNodes.map(nodeAddress ⇒ {
        val container = byAddress(nodeAddress)
        Cell(callback(container))
      }))
    }

    def buildHours: List[Row] = {
      hours.toList.sorted.map { fdHour: FdHour ⇒
        val digestAndContainers = orderedNodes.map { nodeAddress ⇒
          val container = byAddress(nodeAddress)
          val maybeDigest: Option[QsoHourDigest] = container.digestForHour(fdHour)
          val qhd: QsoHourDigest = maybeDigest match {
            case Some(qhd: QsoHourDigest) ⇒
              qhd
            case None ⇒
              QsoHourDigest(fdHour, "--", 0)
          }
          qhd -> container
        }

        HourRow(fdHour.toCell, digestAndContainers)
      }
    }

    val rows: List[Row] = List(
      buildRow("HTTP", ns => Cell(ns.nodeAddress.hostName)
        .withUrl(ns.nodeAddress.url.toExternalForm)),
      buildRow("Age", ns => Cell().asColoredAge(ns.nodeStatus.stamp)),
      buildRow("QSOs", ns => Cell(ns.nodeStatus.qsoCount).withToolTip(
        """How many QSO stored at this node.
          |This should be the same accross all nodes in the cluster.""".stripMargin)),
      buildRow("Journal", ns => Cell(ns.nodeStatus.maybeJournal.map(_.journalFileName).getOrElse("--"))
        .withToolTip(
          """Name of the file that where QSo are journaled.
            |This should be the same accross all nodes in the cluster.
            |""".stripMargin)),
      buildRow("Band", _.nodeStatus.currentStation.bandName),
      buildRow("Mode", _.nodeStatus.currentStation.modeName),
      buildRow("Operator", _.nodeStatus.currentStation.operator),
      buildRow("Rig", _.nodeStatus.currentStation.rig),
      buildRow("Antenna", _.nodeStatus.currentStation.antenna),
    ) ++ buildHours

    data.clear()
    data.addAll(rows: _*)

    def buildColumns: Seq[TableColumn[Row, Cell]] = {
      val colTexts: List[String] = orderedNodes.map(_.display)

      colTexts.zipWithIndex.map(e ⇒
        new TableColumn[Row, Cell] {
          sortable = false
          val colIndex = e._2
          text = e._1
          cellValueFactory = { x: TableColumn.CellDataFeatures[Row, Cell] ⇒
            val row: Row = x.value
            val r: Cell = row.cells(colIndex)
            new ObjectProperty(row, "row", r)
          }

          cellFactory = { _: TableColumn[Row, Cell] =>
            new FdClusterTableCell[Row]
          }
        }
      )
    }

    val rowHeaderCol = new TableColumn[Row, Cell] {
      text = "Node"
      cellFactory = { _: TableColumn[Row, Cell] =>
        new FdClusterTableCell[Row]
      }
      cellValueFactory = { q: TableColumn.CellDataFeatures[Row, Cell] ⇒
        val r = new ObjectProperty(q.value, name = "rowHeader", q.value.rowHeader)
        r
      }
      sortable = false
    }

    onFX {
      tableView.columns.clear()
      tableView.columns += rowHeaderCol
      buildColumns.foreach(tc ⇒
        tableView.columns += tc
      )
    }
  }
}


