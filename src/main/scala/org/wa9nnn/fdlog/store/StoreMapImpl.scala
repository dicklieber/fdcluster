/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.UUID

import com.google.inject.Inject
import org.wa9nnn.fdlog.model.Contact.CallSign
import org.wa9nnn.fdlog.model.NodeInfo.Node
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.util.StructuredLogging
import play.api.libs.json.{JsValue, Json}
import resource._

import scala.collection.concurrent.TrieMap
import scala.io.Source
import org.wa9nnn.fdlog.model.Contact._

class StoreMapImpl @Inject()(@Inject() nodeInfo: NodeInfo, @Inject() currentStationProvider: CurrentStationProvider) extends Store with StructuredLogging {
  implicit val node: Node = nodeInfo.node
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

  private val homeDir = Paths.get(Option(System.getProperty("user.home")).foldLeft("") { (a, v) ⇒ a + v })
  val journalDir: Path = homeDir.resolve("fdlog")
  Files.createDirectories(journalDir)
  private val url: Path = journalDir.resolve("journal.log")
  private val absolutePath: Path = url.toAbsolutePath
  println(s"absolutePath: ${absolutePath.toString}")
  private val outputStream: OutputStream = Files.newOutputStream(url, StandardOpenOption.APPEND, StandardOpenOption.CREATE)

  def load(): Unit = {
    var count = 0
    managed(Source.fromFile(url.toUri)) acquireAndGet { bufferedSource ⇒
      bufferedSource.getLines().foreach { line: String ⇒

        Json.parse(line).asOpt[QsoRecord].foreach { qsoRecord ⇒
          addRecord(qsoRecord)
          count = count + 1
        }
      }
      println(s"loaded $count records.")
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
   * @return None if added, otherwise [[Contact]] that this is a dup of.
   */
  override def add(potentialQso: Qso): Option[QsoRecord] = {
    findDup(potentialQso) match {
      case dup@Some(_) =>
        dup
      case None =>
        val newRecord = QsoRecord(nodeInfo.contest, currentStationProvider.stationContext.ourStation, potentialQso, nodeInfo.fdLogId)
        addRecord(newRecord)
        val jsValue = Json.toJson(newRecord)
        writeJournal(jsValue)
        None
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

  override def dump: Seq[QsoRecord]

  = contacts.values.toSeq.sorted

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
      val maybeExisting = contacts.putIfAbsent(contact.uuid, contact)
      if (logger.isDebugEnabled) {
        (maybeExisting match {
          case None ⇒
            logJson("merged")
          case Some(_) ⇒
            logJson("exists")

        })
          .field("uuid", contact.uuid)
          .field("worked", contact.callsign)
          .debug()
      }
    }
    }
  }
}