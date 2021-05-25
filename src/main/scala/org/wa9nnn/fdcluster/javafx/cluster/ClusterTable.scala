package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.tableui.Cell
import javafx.scene.Node
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.ValuesForNode
import org.wa9nnn.fdcluster.javafx.cluster.PropertyNames.rows
import org.wa9nnn.fdcluster.model.NodeAddress
import scalafx.scene.layout.GridPane

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

@Singleton
class ClusterTable @Inject()(implicit nodeAddress: NodeAddress) extends GridPane {
  styleClass += "clusterTable"
  val nodeColumns = new NodeColumns()

  def update(values: ValuesForNode): Unit = {

    if (nodeColumns.update(values))
      updateGridLayout()

  }

  this.getRowConstraints

  def updateGridLayout() {
    onFX {
      children.clear()

      rows.foreach { case (name, row) =>
        add(new PropertyCell(name, name.getDisplay) {
          styleClass += "clusterRowHeader"
        }, 0, row)
      }

      for {
        col: CellsForNode <- nodeColumns.cols
        row <- rows
      } {
        val propertyCell: PropertyCell = col.cellValue(row._1)
        add(propertyCell, col.column, row._2)
      }
    }
  }

}

/**
 * Hold the [[PropertyCell]]s for a node.
 * Values for the cells can be updated when a new [[org.wa9nnn.fdcluster.model.sync.NodeStatus]] is received.
 *
 * @param nodeAddress from whom we got this.
 */
class NodeColumns(implicit nodeAddress: NodeAddress) {
  val map = new TrieMap[NodeAddress, CellsForNode]()

  /**
   *
   * @param valuesForNode from a [[NodeStatus]]
   * @return new column that needs to be layed out..
   */
  def update(valuesForNode: ValuesForNode): Boolean = {
    var changedLayout = false
    val r: CellsForNode = map.getOrElseUpdate(valuesForNode.nodeAddress, {
      val usedColumnIndices: List[Int] = map.values.map(_.column).toList
      var nextAvailableColumn = 1
      while (usedColumnIndices.contains(nextAvailableColumn)) {
        nextAvailableColumn += 1
      }
      changedLayout = true
      CellsForNode(valuesForNode.nodeAddress, nextAvailableColumn)

    })
    r.update(valuesForNode)
    changedLayout
  }

  /**
   * Used to layout the cells in a grid.
   *
   * @return
   */
  def cols: Iterable[CellsForNode] = {
    map.values
  }
}

/**
 *
 * @param values to get names, setup cells and set initial values.
 * @param column where in the grid.
 */
case class CellsForNode(nodeAddress: NodeAddress, column: Int)(implicit ourNodeAddress: NodeAddress) {
  val map: TrieMap[ValueName, PropertyCell] = new TrieMap[ValueName, PropertyCell]()

  private val initialValue = if (nodeAddress == ourNodeAddress)
    Cell("Us").withStyle("ourNode")
  else
    ""
  map.put(PropertyNames.colHeaderName,
    new PropertyCell(PropertyNames.colHeaderName, initialValue)
  )
  ValueName.values().foreach(n => map.put(n, new PropertyCell(n)))


  def cellValue(name: ValueName): PropertyCell = {
    map.getOrElse(name, new PropertyCell(name,s"No cell for name: $name"))
  }

  def update(namedValues: ValuesForNode): Unit = {
    namedValues.result.foreach { nv =>
      try {
        map(nv.name).update(nv.value)
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }
  def cleanup:Unit = {
    map.values.foreach(_.cleanup())
  }
}


