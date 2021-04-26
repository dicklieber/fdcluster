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

import akka.actor.ActorRef
import com.google.inject.name.Named
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.contest.{Journal, JournalManager, JournalProperty}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHour}
import org.wa9nnn.fdcluster.store
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.util.{StructuredLogging, UuidUtil}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.security.{MessageDigest, SecureRandom}
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

/**
 * This can only be used within the [[StoreActor]]
 *
 */
@Singleton
class StoreLogic @Inject()(na: NodeAddress,
                           @Named("qsoMetadata") qsoMetadata: ObjectProperty[QsoMetadata],
                           @Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                           @Named("multicastSender") multicastSender: ActorRef,
                           contestProperty: ContestProperty,
                           journalManager: JournalManager,
                           journalProperty: JournalProperty
                          )
  extends StructuredLogging with DefaultInstrumented {
  def sendNodeStatus(): Unit = {
    val status = nodeStatus
    multicastSender ! JsonContainer(status)
  }


  def filterAlreadyPresent(iterator: Iterator[Uuid]): Iterator[Uuid] = {
    iterator.filterNot(uuid => byUuid.contains(uuid))
  }

  implicit val nodeAddress: NodeAddress = na

  /**
   * This is the canonical set of data. If its not here then we know we have to also add to [[byCallsign]] and [[allQsos]]
   */
  private val byUuid = new TrieMap[Uuid, QsoRecord]()
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]


  //   override lazy val metricBaseName = MetricName("Store")
  private val random = new SecureRandom()

  def debugKillRandom(nToKill: Int): Unit = {
    for (_ ← 0 until nToKill) {
      val targetIndex = random.nextInt(allQsos.size)
      val targetQsoRecord = allQsos.remove(targetIndex)
      logger.debug(s"Deleting ${targetQsoRecord.display}")
      val targetCallsign = targetQsoRecord.callsign
      byCallsign.remove(targetCallsign).foreach { set ⇒
        val remainder = set - targetQsoRecord
        if (remainder.nonEmpty) {
          byCallsign.put(targetCallsign, remainder)
        }
      }
      byUuid.remove(targetQsoRecord.qso.uuid)
    }
  }

  private val qsoMeter = metrics.meter("qso")
  metrics.gauge("qso count") {
    allQsos.size
  }
  private val qsosDigestTimer = metrics.timer("qsos digest")
  private val hourDigestsTimer = metrics.timer("hours digest")
  private var loadingIndicesFlag = false

  /**
   * This will also add to journal on disk; unless duplicate.
   *
   * @param qsoRecord from log or another node
   * @return [[Dup]] is already in this node. [[Added]] if new to this node.
   */
  def addRecord(qsoRecord: QsoRecord): AddResult = {
    if (loadingIndicesFlag) {
      throw new IllegalStateException("Busy loading indices")
    }
    insertQsoRecord(qsoRecord) match {
      case Some(_) ⇒
        Dup(qsoRecord)
      case None ⇒
        journalManager.write(qsoRecord)
        Added(qsoRecord)
    }
  }

  /**
   * adds a [[QsoRecord]] unless it already is added.
   *
   * @param qsoRecord to be added
   * @return [[None]] if this is new, [[Some[QsoRecord]]] if uuid already exists
   */
  private def insertQsoRecord(qsoRecord: QsoRecord): Option[QsoRecord] = {
    if (loadingIndicesFlag) {
      throw new IllegalStateException("Busy loading indices")
    }
    val maybeExisting = byUuid.putIfAbsent(qsoRecord.qso.uuid, qsoRecord)
    if (maybeExisting.isEmpty) {
      allQsos.add(qsoRecord)
      val callSign = qsoRecord.qso.callSign
      val qsoRecords: Set[QsoRecord] = byCallsign.getOrElse(callSign, Set.empty) + qsoRecord
      byCallsign.put(callSign, qsoRecords)
    }
    maybeExisting
  }

  /**
   * Only for loadLocalIndices
   *
   * @param qsoRecord not already in indices!
   */
  private def localInsert(qsoRecord: QsoRecord): Unit = {
    val maybeExisting = byUuid.putIfAbsent(qsoRecord.qso.uuid, qsoRecord)
    maybeExisting match {
      case Some(_) =>
        logger.error(s"Already have uuid of $qsoRecord")
      case None =>
        val callSign = qsoRecord.qso.callSign
        val qsoRecords: Set[QsoRecord] = byCallsign.getOrElse(callSign, Set.empty) + qsoRecord
        byCallsign.put(callSign, qsoRecords)
    }
  }

  /**
   * Invoked when we have loaded the journal, if any.
   */
  def loadLocalIndices(): Unit = {
    loadingIndicesFlag = true
    allQsos.foreach { qso =>
      localInsert(qso)
    }
    loadingIndicesFlag = false
  }

  /**
   * Add this qso if not a dup.
   *
   * @param potentialQso that may be added.
   * @return None if added, otherwise [[MessageFormats]] that this is a dup of.
   */
  def add(potentialQso: Qso): AddResult = {
    findDup(potentialQso) match {
      case Some(duplicateRecord) =>
        Dup(duplicateRecord)
      case None =>
        val newRecord = QsoRecord(qso = potentialQso,
          qsoMetadata = qsoMetadata.value)
        qsoMeter.mark()
        addRecord(newRecord)
    }
  }

  def findDup(potentialQso: Qso): Option[QsoRecord] = {
    for {
      contacts <- byCallsign.get(potentialQso.callSign)
      dup ← contacts.find(_.dup(potentialQso))
    } yield {
      dup
    }
  }

  def search(search: Search): SearchResult = {
    val max = search.max
    val matching = byUuid.values.filter { qsoRecord => {
      qsoRecord.qso.callSign.contains(search.partial) && search.bandMode == qsoRecord.qso.bandMode
    }
    }.toSeq

    val limited = matching.take(max)
    SearchResult(limited, matching.length)
  }

  /**
   *
   * @return ids of all nodes known to this node.
   */
  def contactIds: NodeQsoIds = {
    store.NodeQsoIds(byUuid.keys.toSet)
  }

  def requestContacts(contactRequest: ContactRequest): NodeDatabase = {

    val selectedContacts = if (contactRequest.contactIds.isEmpty) {
      byUuid.values
    } else {
      contactRequest.contactIds.flatMap(byUuid.get)
    }
    NodeDatabase(selectedContacts.toSeq)
  }

  def merge(qsoRecords: Seq[QsoRecord]): Unit = {

    try {
      var mergeCount = 0
      var existedCount = 0
      qsoRecords.foreach { qsoRecord ⇒ {
        addRecord(qsoRecord) match {
          case Added(_) ⇒
            mergeCount = mergeCount + 1
          case Dup(_) ⇒
            existedCount = existedCount + 1
        }
      }
      }
    } catch {
      case eT: Throwable ⇒
        logger.error("merge", eT)

    }
  }

  def size: Int = byUuid.size

 private  def nodeStatus: NodeStatus = {
    //todo Needs some serious caching. Past hours don't usually change (unless syncing)

    val sDigest = qsosDigestTimer.time {
      val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
      byUuid.values.foreach(qr ⇒ messageDigest.update(UuidUtil(qr.qso.uuid)))
      val bytes = messageDigest.digest()
      val encoder = java.util.Base64.getEncoder
      val bytes1 = encoder.encode(bytes)
      new String(bytes1)
    }

    val hourDigests = hourDigestsTimer.time {
      byUuid
        .values
        .toList
        .groupBy(_.fdHour)
        .values.map(QsoHour(_))
        .map(_.hourDigest).toList
        .sortBy(_.fdHour)
    }
    val rate = qsoMeter.fifteenMinuteRate
    val currentStation = CurrentStation()

    sync.NodeStatus(nodeAddress, byUuid.size, sDigest, hourDigests, qsoMetadata.value, currentStation, rate, contestProperty.value,
      journal = journalProperty.maybeJournal)

  }

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  def uuidForHour(fdHour: FdHour): List[Uuid] = {
    allQsos.filter((qsr: QsoRecord) => qsr.fdHour == fdHour)
      .map(_.qso.uuid)
      .toList
  }


  def get(fdHour: FdHour): List[QsoHour] = {
    byUuid.values
      .toList
      .sorted.groupBy(_.fdHour).values.map(QsoHour(_))
      .filter(_.fdHour == fdHour)
      .toList
  }

  def getQsos(fdHour: FdHour): List[QsoRecord] = {
    byUuid.values.filter(_.fdHour == fdHour).toList
  }


  def get(uuid: Uuid): Option[QsoRecord] = {
    byUuid.get(uuid)
  }

  def debugClear(): Unit = {
    logger.info(s"Clearing this nodes store for debugging!")
    byUuid.clear()
    byCallsign.clear()
    allQsos.clear()
  }

//  def missingUuids(uuidsAtOtherHost: List[Uuid]): List[Uuid] = {
//    uuidsAtOtherHost.filter(otherUuid ⇒
//      !byUuid.contains(otherUuid)
//    )
//  }
}

sealed trait AddResult

case class Added(qsoRecord: QsoRecord) extends AddResult

case class Dup(qsoRecord: QsoRecord) extends AddResult

