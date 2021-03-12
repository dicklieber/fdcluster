package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.MessageFormats.Uuid
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.model.{Qso, QsoRecord, QsosFromNode}
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
   * find potential matches by callsign
   *
   * @param search some or all of a callsign and BandMode
   * @return matches matches.
   */
  def search(search:Search): SearchResult

  /**
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  def contactIds: NodeQsoIds

  def dump: QsosFromNode

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
