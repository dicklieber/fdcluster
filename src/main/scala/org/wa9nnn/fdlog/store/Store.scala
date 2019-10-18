package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.MessageFormats.{CallSign, Uuid}
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.NodeStatus
import org.wa9nnn.fdlog.store.network.FdHour

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
   * find potential matches by callsign
   *
   * @param in some or all of a callsign.
   * @return potneital matches.
   */
  def search(in: CallSign): Seq[QsoRecord]

  /**
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  def contactIds: NodeQsoIds

  def dump: Seq[QsoRecord]

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  def uuidForHours(fdHours: Set[FdHour]): Seq[Uuid]

  def size: Int

  def nodeStatus: NodeStatus

  def debugClear(): Unit

}

sealed trait AddResult

case class Added(qsoRecord: QsoRecord) extends AddResult

case class Dup(qsoRecord: QsoRecord) extends AddResult
