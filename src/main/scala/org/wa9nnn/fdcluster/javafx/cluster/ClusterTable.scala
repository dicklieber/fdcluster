package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import scalafx.scene.layout.GridPane

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

@Singleton
class ClusterTable @Inject()(nodeColumns: NodeColumns) extends GridPane {
  styleClass += "clusterTable"

  def update(nodeStatus: NodeStatus): Unit = {
    if (nodeColumns.update(nodeStatus) )
      updateGridLayout()
  }

  this.getRowConstraints

  def updateGridLayout() {
    onFX {
      children.clear()
      // row headers
      val namesWithIndex = ValueName.values().zipWithIndex
      namesWithIndex.foreach { case (propertyCellName, row) =>
        add(new PropertyCell(propertyCellName, propertyCellName.name) {
          styleClass += "clusterRowHeader"
        }, 0, row)
      }
      // node values e.g. body
      for {
        (cells: NodeCells, col) <- nodeColumns.nodeCells.zipWithIndex
        (name, row) <- namesWithIndex
      } {
        val propertyCell: PropertyCell = cells.getCell(name)
        add(propertyCell, col + 1, row)
      }
    }
  }

}






