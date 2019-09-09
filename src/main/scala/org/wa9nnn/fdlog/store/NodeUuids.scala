/*
 * Copyright (C) 2017  Dick Lieber, WA9NNN
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wa9nnn.fdlog.store

import java.time.Instant

import org.wa9nnn.fdlog.model.MessageFormats.Uuid
import org.wa9nnn.fdlog.model.{NodeAddress, QsoRecord}
import org.wa9nnn.fdlog.store.NodeInfo.Node

/**
 * Ids on a node.
 *
 * @param contactIds ids on the node.
 * @param node       where this came from.
 * @param stamp      as of.
 *
 */
case class NodeUuids(contactIds: Set[Uuid], node: NodeAddress, stamp: Instant)

object NodeUuids {
  def apply(uuids: Set[Uuid] = Set.empty[Uuid])(implicit node: NodeAddress): NodeUuids = {
    new NodeUuids(uuids, node, Instant.now())
  }
}


case class ContactRequest(contactIds: Set[Uuid], requestingNode: Node)

object NodeIuids {
  def apply(contactIds: Set[Uuid] = Set.empty[Uuid])(implicit node: Node): ContactRequest = {
    ContactRequest(contactIds, node)
  }
}

/**
 * Usually this will be in response to a ContactRequest request.
 * when a new, empty, node joins he cluster
 *
 * @param stamp    as of.
 * @param node     where this came from.
 * @param records  on node.
 */
case class NodeDatabase(records: Seq[QsoRecord], node: NodeAddress, stamp: Instant = Instant.now())

object NodeDatabase {
  def apply(contacts: Seq[QsoRecord])(implicit node: NodeAddress): NodeDatabase = NodeDatabase(contacts, node)
}

/**
 *
 * @param qsoRecord just added record
 * @param qsoCount number of QsoRecords in the sending node.
 */
case class NewQso(qsoRecord: QsoRecord, qsoCount:Int)
