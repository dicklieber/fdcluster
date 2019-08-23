/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.{Duration, Instant}
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats.{CallSign, _}
import org.wa9nnn.fdlog.model._
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
class StoreMapImpl(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, journalFilePath: Path)
  extends Store with LazyLogging {
  implicit val node: Node = nodeInfo.nodeAddress
  private val contacts = new TrieMap[UUID, QsoRecord]()
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


  //  private val homeDir = Paths.get(Option(System.getProperty("user.home")).foldLeft("") { (a, v) ⇒ a + v })
  val journalDir: Path = journalFilePath.getParent
  Files.createDirectories(journalDir)
  logger.info(s"journal: ${journalFilePath.toAbsolutePath.toString}")
  private val outputStream: OutputStream = Files.newOutputStream(journalFilePath, StandardOpenOption.APPEND, StandardOpenOption.CREATE)

  def load(): Unit = {
    var count = 0
    val start = Instant.now()
    managed(Source.fromFile(journalFilePath.toUri)) acquireAndGet { bufferedSource ⇒
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
  }

  load()

  def writeJournal(jsValue: JsValue): Unit = {
    val lineOfJson = jsValue.toString()

    outputStream.write(lineOfJson.getBytes())
    outputStream.write("\n".getBytes())
    outputStream.flush()
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

    contactFromAnotherNode.records.foreach { contact ⇒ {
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
}