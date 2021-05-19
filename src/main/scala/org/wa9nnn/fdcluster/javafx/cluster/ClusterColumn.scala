package org.wa9nnn.fdcluster.javafx.cluster

import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.{CellProperty, NamedValue, NamedValueCollector, ValueName}
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState.NodeStatusProperty
import scalafx.geometry.Orientation
import scalafx.scene.layout.{TilePane, VBox}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class ClusterColumn(val nodeStatusProperty: NodeStatusProperty) extends TilePane {
  orientation = Orientation.Vertical

  private val ns: NodeStatus = nodeStatusProperty.value

  def collect(ns: NodeStatus): List[CellProperty] = {
    val namedValueCollector = NamedValueCollector()
    namedValueCollector(NamedValue(ValueName(ns.getClass, "Stamp"), ns.stamp))
    ns.collectNamedValues(namedValueCollector)
    namedValueCollector.result.map(CellProperty(_))
  }


  val cellMap = new TrieMap[ValueName, CellProperty]
  private val cells: List[CellProperty] = collect(ns)
  cells.foreach(cp => cellMap.put(cp.name, cp))


  children = cells.sorted


  nodeStatusProperty.onChange { (_, _, ns) =>
    onFX {
      val newValues = collect(ns)
      newValues.foreach { cp =>
        cellMap(cp.name).value(cp.value)
      }
    }
  }
}
