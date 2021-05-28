package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import scalafx.scene.layout.GridPane

import javax.inject.{Inject, Singleton}

@Singleton
class ClusterTable @Inject()(nodeColumns: NodeColumns) extends GridPane {
  styleClass += "clusterTable"

  def update(nodeStatus: NodeStatus): Unit = {
    if (nodeColumns.update(nodeStatus))
      updateGridLayout()
  }

  this.getRowConstraints

  def updateGridLayout() {
    onFX {
      children.clear()
      // row headers
      val namesWithIndex = ValueName.values().zipWithIndex
      namesWithIndex.foreach { case (propertyCellName, row) =>
        add(SimplePropertyCell(propertyCellName,
          Cell(propertyCellName.name)
          .withCssClass("clusterRowHeader")),
          0, row)
      }
      // node values e.g. body
      for {
        (cells: NodeCells, col) <- nodeColumns.nodeCells.zipWithIndex
        (name, row) <- namesWithIndex
      } {
        val propertyCell: PropertyCell[_] = cells.getCell(name)
        add(propertyCell, col + 1, row)
      }
    }
  }

}






