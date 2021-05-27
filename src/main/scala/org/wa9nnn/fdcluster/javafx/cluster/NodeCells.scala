package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.FdHour

import scala.collection.concurrent.TrieMap

/**
 * Holds the [[PropertyCell]]s for a node.
 *
 * @param nodeAddress who this is for.
 * @param knownHours  so we have a cell for hour.
 */
case class NodeCells(nodeAddress: NodeAddress, fdHours: FdHours, ourNode: NodeAddress) extends LazyLogging {
  private val cells: TrieMap[PropertyCellName, PropertyCell] = TrieMap[PropertyCellName, PropertyCell]()

  /**
   *
   * @param nodeStatus incoming.
   */
  def update(nodeStatus: NodeStatus): Unit = {
    assert(nodeStatus.nodeAddress == nodeAddress, "Mis-match nodeAddress!")
    nodeStatus.values.foreach { namedValue =>
      try {
        cells.getOrElseUpdate(namedValue.name, {
          new PropertyCell(namedValue.name)
        }).update(namedValue.value)
      }
    }
  }


  /**
   *
   * @param propertyCellName of interest.
   * @return always the matching [[PropertyCell]].
   * @throws NoSuchElementException on missing PropertyCell.
   */
  def getCell(propertyCellName: PropertyCellName): PropertyCell = {
    cells(propertyCellName)
  }
}
