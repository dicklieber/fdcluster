package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import javafx.collections.ObservableMap
import javafx.scene.Node
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import scalafx.collections.ObservableBuffer
import scalafx.scene.layout.GridPane

import javax.inject.{Inject, Singleton}

@Singleton
class ClusterTable @Inject()(nodeColumns: NodeColumns) extends GridPane with LazyLogging {
  styleClass += "clusterTable"

  def update(nodeStatus: NodeStatus): Unit = {
    if (nodeColumns.update(nodeStatus))
      updateGridLayout()
  }

  def purge(deadNodes: List[NodeAddress]): Unit = {
    nodeColumns.purge(deadNodes)
    updateGridLayout()
  }

  this.getRowConstraints

  def updateGridLayout() {
    logger.debug("updateGridLayout")
    onFX {
      children.clear()
      // row headers
      val namesWithIndex = ValueName.values().zipWithIndex
      namesWithIndex.foreach { case (propertyCellName, iRow) =>
        add(PropertyCellFactory(propertyCellName,
          Cell(propertyCellName.name)
            .withCssClass("clusterRowHeader")),
          0, iRow)
      }
      // node values e.g. body
      for {
        (cells: NodeCells, col) <- nodeColumns.nodeCells.zipWithIndex
        (name, row) <- namesWithIndex
      } {
        try {
          val propertyCell: PropertyCell = cells.getCell(name)
          add(propertyCell, col + 1, row)
        } catch {
          case e: NoSuchElementException =>
            logger.debug(s"$name: name", e)
        }
      }

      logger.whenDebugEnabled {
        val buffer: ObservableBuffer[Node] = children
        buffer.foreach((node: Node) => {
          node match {
            case bp: javafx.scene.layout.BorderPane =>
            case x =>
              logger.info(s"x: $x")
          }
        }
        )
      }

    }
  }

}






