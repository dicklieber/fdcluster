
package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.MessageFormats.Uuid
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.FdHour

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
    val qsoRecord = QsoRecord(potentialQso, QsoMetadata())
    qsos.append(qsoRecord)
     Added(qsoRecord)
  }

  /**
   * Insert a QsoRecord
   *
   * @param qsoRecord from another node or for testing.
   */
  override def addRecord(qsoRecord: QsoRecord): AddResult = throw new NotImplementedError()

  /**
   * find potential matches by callSign
   *
   * @param search some or all of a callSign.
   * @return potneital matches.
   */
  override def search(search: Search): SearchResult = throw new NotImplementedError()

  /**
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  override def contactIds: NodeQsoIds = throw new NotImplementedError()

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

