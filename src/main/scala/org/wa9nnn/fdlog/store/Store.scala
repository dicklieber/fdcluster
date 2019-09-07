package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.MessageFormats.CallSign
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.NodeStatus

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
  def addRecord(qsoRecord: QsoRecord)

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
  def contactIds: NodeUuids

  def dump: Seq[QsoRecord]

  def size:Int

  def nodeStatus:NodeStatus

}

sealed trait AddResult

case class Added(qsoRecord: QsoRecord) extends AddResult
case class Dup(qsoRecord: QsoRecord) extends AddResult
