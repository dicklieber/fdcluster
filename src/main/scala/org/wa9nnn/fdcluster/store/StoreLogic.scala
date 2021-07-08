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
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.contest.{JournalProperty, JournalWriter}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{BaseNodeStatus, NodeStatus, QsoHour}
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.webclient.{ListSessions, Session}
import scalafx.collections.ObservableBuffer

import java.security.SecureRandom
import java.util.UUID
import javax.inject.{Inject, Named, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * This can only be used within the [[StoreActor]]
 *
 */
@Singleton
class StoreLogic @Inject()(na: NodeAddress,
                           contestProperty: ContestProperty,
                           stationProperty: StationProperty,
                           journalLoader: JournalLoader,
                           val journalWriter: JournalWriter,
                           journalManager: JournalProperty,
                           val listeners: immutable.Set[AddQsoListener],
                           storeSender: StoreSender,
                           qsoBuffer: ObservableBuffer[Qso],
                           @Named("sessionManager") sessionManager: ActorRef
                          )
  extends LazyLogging with QsoSource {
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  /*
  QSOs live in these three structures. Since Qso is immutable all three structures are simply references.
   */
  /**
   * qsoBuffer is shared read-only by DataScene
   */
  val byUuid = new TrieMap[UUID, Qso]()
  val byCallSign = new TrieMap[CallSign, Set[Qso]]

  /**
   * Add to memory. Does not persist to journal.
   *
   * @param candidateQso of interest.
   * @return
   */
  def ingest(candidateQso: Qso): Try[Qso] = {
    Try {
      val maybeQso = byUuid.get(candidateQso.uuid)
      maybeQso.foreach(_ =>
        throw new UuidDup()
      )
      checkDup(candidateQso)
      qsoBuffer.add(candidateQso)
      byUuid.put(candidateQso.uuid, candidateQso)
      val callSign = candidateQso.callSign
      val qsos: Set[Qso] = byCallSign.getOrElse(callSign, Set.empty) + candidateQso
      byCallSign.put(callSign, qsos)
      listeners.foreach(_.add(candidateQso))
      candidateQso
    }
  }

  def ingestAndPersist(candidateQso: Qso): Try[Qso] = {
    for {
      qso <- ingest(candidateQso)
      q <- Try(journalWriter.write(qso))
    } yield {
      q
    }
  }

//  def sendNodeStatus(): Unit = {
//    multicastSender ! JsonContainer(nodeStatus)
//  }

  def filterAlreadyPresent(iterator: Iterator[Uuid]): Iterator[Uuid] = {
    iterator.filterNot(uuid => byUuid.contains(uuid))
  }

  implicit val nodeAddress: NodeAddress = na

  private val random = new SecureRandom()

  def debugKillRandom(nToKill: Int): Unit = {
    for (_ ← 0 until nToKill) {
      val targetIndex = random.nextInt(qsoBuffer.size)
      val targetQso: Qso = qsoBuffer.remove(targetIndex)
      logger.debug(s"Deleting ${targetQso.display}")
      val targetCallSign = targetQso.callSign
      byCallSign.remove(targetCallSign).foreach { set ⇒
        val remainder = set - targetQso
        if (remainder.nonEmpty) {
          byCallSign.put(targetCallSign, remainder)
        }
      }
      byUuid.remove(targetQso.uuid)
    }
  }

  /**
   *
   * @param candidateQso that could be a dup.
   * @throws DupContact if duplicate contact found.
   */
  def checkDup(candidateQso: Qso): Unit = {
    for {
      contacts <- byCallSign.get(candidateQso.callSign)
      _ ← contacts.find(_.isDup(candidateQso))
    } yield {
      throw new DupContact(candidateQso)
    }
  }

  def search(search: Search): SearchResult = {
    logger.whenTraceEnabled {
      logger.trace(s"Search in: $search")
    }
    if (search.partial.isEmpty) {
      SearchResult(Seq.empty, 0, search)
    } else {
      val max = search.max
      val matching = byUuid.values.filter { qso => {
        qso.callSign.contains(search.partial) && search.bandMode == qso.bandMode
      }
      }.toSeq

      val limited = matching.take(max)
      val result = SearchResult(limited, matching.length, search)
      logger.whenTraceEnabled {
        logger.trace(s"Search out: $result")
      }
      result
    }
  }

  def size: Int = byUuid.size

  def nodeStatus: NodeStatus = {
    //todo Needs some serious caching. Past hours don't usually change (unless syncing)

    val hourDigests =
      byUuid
        .values
        .toList
        .groupBy(_.fdHour)
        .values.map(QsoHour(_))
        .map(_.hourDigest).toList
        .sortBy(_.fdHour)

    val eventualSessions: Future[List[Session]] = (sessionManager ? ListSessions).mapTo[List[Session]]
    val value: Try[List[Session]] = Try(Await.result[List[Session]](eventualSessions, timeout.duration))

    NodeStatus(BaseNodeStatus(
      nodeAddress = nodeAddress,
      qsoCount = byUuid.size,
      qsoHourDigests = hourDigests,
      station = stationProperty.value,
      contest = contestProperty.exportValue,
      sessions = value.getOrElse(List.empty),
      journal = journalManager.exportValue))
  }


  def uuidForHour(fdHour: FdHour): List[Uuid] = {
    qsoBuffer.filter((qsr: Qso) => qsr.fdHour == fdHour)
      .map(_.uuid)
      .toList
  }

  def get(fdHour: FdHour): List[QsoHour] = {
    byUuid.values
      .toList
      .sorted.groupBy(_.fdHour).values.map(QsoHour(_))
      .filter(_.fdHour == fdHour)
      .toList
  }

  def getQsos(fdHour: FdHour): List[Qso] = {
    byUuid.values.filter(_.fdHour == fdHour).toList
  }


  def get(uuid: Uuid): Option[Qso] = {
    byUuid.get(uuid)
  }

  def clear(): Unit = {
    logger.info(s"Clearing this nodes store")
    byUuid.clear()
    byCallSign.clear()
    qsoBuffer.clear()
    listeners.foreach(_.clear())

  }

  //  def missingUuids(uuidsAtOtherHost: List[Uuid]): List[Uuid] = {
  //    uuidsAtOtherHost.filter(otherUuid ⇒
  //      !byUuid.contains(otherUuid)
  //    )
  //  }

  override def qsoIterator: Iterable[Qso] = byUuid.values

  journalLoader.startLoad((qso: Qso) => {
    ingest(qso)
  })
  storeSender ! BufferReady
}

case class AddResult(triedQso: Try[Qso])

trait AddQsoListener {
  def add(Qso: Qso): Unit

  def clear(): Unit
}

trait QsoSource {
  def qsoIterator: Iterable[Qso]
}


class UuidDup extends Exception("Already have UUID!")

class DupContact(otherQso: Qso) extends Exception(s"Dup with ${otherQso.callSign}")
