/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.time.{Duration, Instant, LocalDate, LocalDateTime, ZoneId}

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats.{CallSign, _}
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHour, QsoHourIds}
import org.wa9nnn.fdlog.store.NodeInfo.Node
import play.api.libs.json.{JsValue, Json}
import resource._

import scala.collection.concurrent.TrieMap
import scala.io.Source

/**
 *
 * @param nodeInfo                    who we are
 * @param currentStationProvider      things that may vary with operator.
 * @param journalFilePath             where journal file lives.
 */
class StoreMapImpl(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, journalFilePath: Option[Path] = None)
  extends Store with LazyLogging {
  implicit val node: Node = nodeInfo.nodeAddress
  private val contacts = new TrieMap[Uuid, QsoRecord]()
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]

  def length: Int = contacts.size

  /**
   * for sync
   *
   * @param qsoRecord from log or another node
   */
  def addRecord(qsoRecord: QsoRecord): Unit = {
    contacts.putIfAbsent(qsoRecord.uuid, qsoRecord)
    val callsign = qsoRecord.qso.callsign
    val qsoRecords: Set[QsoRecord] = byCallsign.getOrElse(callsign, Set.empty) + qsoRecord
    byCallsign.put(callsign, qsoRecords)
  }

  private val outputStream: Option[OutputStream] = journalFilePath.map { path ⇒

    var count = 0
    val start = Instant.now()
    managed(Source.fromFile(path.toUri)) acquireAndGet { bufferedSource ⇒
      bufferedSource.getLines().foreach { line: String ⇒

        Json.parse(line).asOpt[QsoRecord].foreach { qsoRecord ⇒
          addRecord(qsoRecord)
          count = count + 1

          if (count > 0 && count % 250 == 0) {
            val seconds = Duration.between(start, Instant.now()).getSeconds
            if (seconds > 0) {
              val qsoPerSecond = count / seconds
              println(f"loaded $count%,d records. ($qsoPerSecond%,d)/per sec")
            }
          }
        }
      }
      val seconds = Duration.between(start, Instant.now()).getSeconds
      if (seconds > 0) {
        val qsoPerSecond = count / seconds
        println(f"loaded $count%,d records. ($qsoPerSecond%,d)/per sec")
      }
    }

    val journalDir: Path = path.getParent
    Files.createDirectories(journalDir)
    logger.info(s"journal: ${path.toAbsolutePath.toString}")

    Files.newOutputStream(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE)

  }

  def writeJournal(jsValue: JsValue): Unit = {
    outputStream.foreach { os ⇒
      val lineOfJson = jsValue.toString()

      os.write(lineOfJson.getBytes())
      os.write("\n".getBytes())
      os.flush()
    }
  }

  /**
   * Add this qso if not a dup.
   *
   * @param potentialQso that may be added.
   * @return None if added, otherwise [[MessageFormats]] that this is a dup of.
   */
  override def add(potentialQso: Qso): AddResult = {
    findDup(potentialQso) match {
      case Some(duplicateRecord) =>
        Dup(duplicateRecord)
      case None =>
        val newRecord = QsoRecord(nodeInfo.contest, currentStationProvider.currentStation.ourStation, potentialQso, nodeInfo.fdLogId)
        addRecord(newRecord)
        val jsValue = Json.toJson(newRecord)
        writeJournal(jsValue)
        Added(newRecord)
    }
  }

  def findDup(potentialQso: Qso): Option[QsoRecord] = {
    for {
      contacts <- byCallsign.get(potentialQso.callsign)
      dup ← contacts.find(_.dup(potentialQso))
    } yield {
      dup
    }
  }

  override def search(in: String): Seq[QsoRecord] = {
    contacts.values.find(_.qso.callsign.contains(in))
    }.toSeq

  override def dump: Seq[QsoRecord] = contacts.values.toSeq.sorted

  /**
   *
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  def contactIds: NodeUuids = {
    NodeUuids(contacts.keys.toSet)
  }

  def requestContacts(contactRequest: ContactRequest): NodeDatabase = {

    val selectedContacts = if (contactRequest.contactIds.isEmpty) {
      contacts.values
    } else {
      contactRequest.contactIds.flatMap(contacts.get)
    }
    NodeDatabase(selectedContacts.toSeq)
  }

  def merge(contactFromAnotherNode: NodeDatabase): Unit = {

    contactFromAnotherNode.records.foreach {
      contact ⇒ {
        //      val maybeExisting = contacts.putIfAbsent(contact.uuid, contact)
        //      if (logger.isDebugEnabled) {
        //        (maybeExisting match {
        //          case None ⇒
        //            logJson("merged")
        //          case Some(_) ⇒
        //            logJson("exists")
        //
        //        })
        //          .field("uuid", contact.uuid)
        //          .field("worked", contact.callsign)
        //          .debug()
        //      }
      }
    }
  }

  override def size: Int = contacts.size

  override def nodeStatus: NodeStatus = {
    val sorted: List[QsoRecord] = contacts.values
      .toList
//      .sortWith { (qr1, qr2) ⇒ qr1.qso.stamp.isBefore(qr2.qso.stamp) }

    val grouped = sorted.groupBy(_.fdHour)
    val v = grouped.values.map(QsoHour(_))
      .map(_.qsoIds)

    val list = v.toList
        .sortBy(_.startOfHour)
    NodeStatus(nodeInfo.nodeAddress, contacts.size, list)
  }

}