
package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.MessageFormats.{CallSign, Uuid}
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.network.FdHour

import java.util.UUID
import scala.collection.mutable.ArrayBuffer

class MockStore extends Store {
  val qsos = new ArrayBuffer[QsoRecord]()
  /**
   * Add this qso if not a dup.
   *
   * @param potentialQso that may be added.
   * @return Added or Dup
   */
  override def add(potentialQso: Qso): AddResult = {
    val fdLogId = new FdLogId(1, NodeAddress())
    val ourStation = OurStation("WM9W")
    val contest = Contest(year = 19)
    val qsoRecord = QsoRecord(potentialQso, contest, ourStation, fdLogId)
    qsos.append(qsoRecord)
    new Added(qsoRecord)
  }

  /**
   * Insert a QsoRecord
   *
   * @param qsoRecord from another node or for testing.
   */
  override def addRecord(qsoRecord: QsoRecord): AddResult = throw new NotImplementedError()

  /**
   * find potential matches by callsign
   *
   * @param in some or all of a callsign.
   * @return potneital matches.
   */
  override def search(in: CallSign): Seq[QsoRecord] = throw new NotImplementedError()

  /**
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  override def contactIds: NodeQsoIds = throw new NotImplementedError()

  override def dump: QsosFromNode = throw new NotImplementedError()

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  override def uuidForHours(fdHours: Set[FdHour]): Seq[Uuid] = throw new NotImplementedError()

  /**
   *
   * @param uuidsAtOtherHost that are present at another host
   * @return uuidsAtOtherHost minus those already at this node.
   */
  override def missingUuids(uuidsAtOtherHost: List[Uuid]): List[Uuid] = throw new NotImplementedError()

  override def size: Int = throw new NotImplementedError()

  override def nodeStatus: NodeStatus = throw new NotImplementedError()

  override def debugClear(): Unit = throw new NotImplementedError()
}

