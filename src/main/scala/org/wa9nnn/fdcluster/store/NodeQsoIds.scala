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
package org.wa9nnn.fdcluster.store

import java.time.Instant
import org.wa9nnn.fdcluster.model.MessageFormats.{Node, Uuid}
import org.wa9nnn.fdcluster.model.{NodeAddress, Qso}

/**
 * Ids on a node.
 *
 * @param qsoIds ids on the node.
 * @param node       where this came from.
 * @param stamp      as of.
 *
 */
case class NodeQsoIds(qsoIds: Set[Uuid], node: NodeAddress, stamp: Instant)

object NodeQsoIds {
  def apply(uuids: Set[Uuid] = Set.empty[Uuid])(implicit node: NodeAddress): NodeQsoIds = {
    new NodeQsoIds(uuids, node, Instant.now())
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
case class NodeDatabase(records: Seq[Qso], node: NodeAddress, stamp: Instant = Instant.now())

object NodeDatabase {
  def apply(contacts: Seq[Qso])(implicit node: NodeAddress): NodeDatabase = NodeDatabase(contacts, node)
}

/**
 *
 * @param Qso just added record
 * @param qsoCount number of Qsos in the sending node.
 */
case class NewQso(Qso: Qso, qsoCount:Int)
