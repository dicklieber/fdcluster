package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus

import scala.collection.concurrent.TrieMap

/**
 * Holds the [[PropertyCell]]s for a node.
 *
 * @param nodeAddress who this is for.
 * @param knownHours  so we have a cell for hour.
 */
case class NodeCells(nodeAddress: NodeAddress, ourNode: NodeAddress) extends LazyLogging {
  private val cells: TrieMap[PropertyCellName, PropertyCell] = TrieMap[PropertyCellName, PropertyCell]()

  /**
   *
   * @param nodeStatus incoming.
   */
  def update(nodeStatus: NodeStatus): Unit = {
    assert(nodeStatus.nodeAddress == nodeAddress, "Mis-match nodeAddress!")
    nodeStatus.values.foreach { namedValue =>
      cells.getOrElseUpdate(namedValue.name, {
        val propertyCell = PropertyCellFactory(namedValue)
        propertyCell.update(namedValue)
        propertyCell
      })

    }
  }

  /**
   *
   * @param propertyCellName of interest.
   * @return always the matching [[PropertyCell]].
   * @throws NoSuchElementException on missing PropertyCell.
   */
  def getCell(propertyCellName: PropertyCellName): PropertyCell = {
    cells.getOrElse(propertyCellName, TextPropertyCell(NamedValue(propertyCellName, "-")))
  }

}


