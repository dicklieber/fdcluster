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

import org.wa9nnn.fdcluster.model.MessageFormats.Uuid
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.model.{Qso, QsoRecord}
import org.wa9nnn.fdcluster.store.network.FdHour

trait Store {

  /**
   * Add this qso if not a dup.
   *
   * @param potentialQso that may be added.
   * @return Added or Dup
   */
  def add(potentialQso: Qso): AddResult

  /**
   * Insert a QsoRecord
   *
   * @param qsoRecord from another node or for testing.
   */
  def addRecord(qsoRecord: QsoRecord): AddResult

  /**
   * find potential matches by callSign
   *
   * @param search some or all of a callSign and BandMode
   * @return matches matches.
   */
  def search(search:Search): SearchResult

  /**
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  def contactIds: NodeQsoIds

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  def uuidForHours(fdHours: Set[FdHour]): Seq[Uuid]

  /**
   *
   * @param uuidsAtOtherHost that are present at another host
   * @return uuidsAtOtherHost minus those already at this node.
   */
  def missingUuids(uuidsAtOtherHost:List[Uuid]): List[Uuid]


  def size: Int

  def nodeStatus: NodeStatus

  def debugClear(): Unit

}

sealed trait AddResult

case class Added(qsoRecord: QsoRecord) extends AddResult

case class Dup(qsoRecord: QsoRecord) extends AddResult
