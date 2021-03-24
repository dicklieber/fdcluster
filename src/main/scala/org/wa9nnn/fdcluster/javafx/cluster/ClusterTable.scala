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

import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.store.network.cluster.NodeStateContainer
import scalafx.beans.property.ObjectProperty
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

    val orderedNodes: List[NodeAddress] = byAddress.keySet.toList.sorted

    /**
     *
     * @param rowHeader string for 1st column
     * @param callback  how to extract body cell from a NodeStateContainer. Will be called for each [[NodeStateContainer]]
     * @return a row for the table
     */
    def buildRow(rowHeader: String, callback: NodeStateContainer ⇒ Any): Row = {
      MetadataRow(StyledAny(rowHeader), orderedNodes.map(nodeAddress ⇒ {
        val container = byAddress(nodeAddress)
        val value = callback(container)
        val fv = if (value.toString == "usOrOther") {
          if (container.isUs) {
            "Us"
          } else {
            "Other"
          }
        } else {
          value
        }

        val sa = StyledAny(fv)
        val r = container.styleForUs(sa)
        r
      }))
    }

    def buildHours: List[Row] = {
      hours.toList.sorted.map { fdHour: FdHour ⇒
        val digestAndContainers = orderedNodes.map { nodeAddress ⇒
          val container = byAddress(nodeAddress)
          val maybeDigest: Option[QsoHourDigest] = container.digestForHour(fdHour)
          val qhd = maybeDigest match {
            case Some(qhd: QsoHourDigest) ⇒
              qhd
            case None ⇒
              QsoHourDigest(fdHour, "--", 0)
          }
          qhd → container
        }

        HourRow(StyledAny(fdHour), digestAndContainers)
      }
    }

    val rows: List[Row] = List(
      buildRow("Location", _ ⇒ "usOrOther"),
      buildRow("Started", _.firstContact),
      buildRow("Last", _.nodeStatus.stamp),
      buildRow("QSOs", _.nodeStatus.qsoCount),
      buildRow("QSO/Minute", _.nodeStatus.qsoRate),
      buildRow("Digest", _.nodeStatus.digest),
      buildRow("Band", _.nodeStatus.bandModeOperator.bandName),
      buildRow("Mode", _.nodeStatus.bandModeOperator.modeName),
      buildRow("Operator", _.nodeStatus.bandModeOperator.operator),
      buildRow("Rig", _.nodeStatus.bandModeOperator.rig),
      buildRow("Antenna", _.nodeStatus.bandModeOperator.antenna),
    ) ++ buildHours

    data.clear()
    data.addAll(rows: _*)

    def buildColumns = {
      val colTexts: List[String] = orderedNodes.map(_.display)

      colTexts.zipWithIndex.map(e ⇒
        new TableColumn[Row, StyledAny] {
          sortable = false
          val colIndex = e._2
          text = e._1
          cellValueFactory = { x: TableColumn.CellDataFeatures[Row, StyledAny] ⇒
            val row: Row = x.value
            val r = row.cells(colIndex)
            new ObjectProperty(row, "row", r)
          }

          cellFactory = { _: TableColumn[Row, StyledAny] =>
            new FdClusterTableCell[Row, StyledAny]
          }
        }
      )
    }

    val rowHeaderCol = new TableColumn[Row, StyledAny] {
      text = "Node"
      cellFactory = { _: TableColumn[Row, StyledAny] =>
        new FdClusterTableCell[Row, StyledAny]
      }
      cellValueFactory = { q ⇒
        val r = new ObjectProperty(q.value, name = "rowHeader", StyledAny(q.value.rowHeader.value))
        r
      }
      sortable = false
    }

    onFX{
      tableView.columns.clear()
      tableView.columns += rowHeaderCol
      buildColumns.foreach(tc ⇒
        tableView.columns += tc
      )
    }
  }
}


