/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.{File, OutputStream, PrintWriter}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.UUID

import javax.inject.Inject
import org.wa9nnn.fdlog.model.Contact.CallSign
import org.wa9nnn.fdlog.model.NodeInfo.Node
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.util.StructuredLogging
import play.api.libs.json.{JsValue, Json}

import scala.collection.concurrent.TrieMap

class StoreMapImpl(nodeInfo: NodeInfo) extends Store with StructuredLogging {
  implicit val node: Node = nodeInfo.node
  private val contacts = new TrieMap[UUID, QsoRecord]()
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]

  def length: Int = contacts.size

  /**
    * for sync
    *
    * @param qsoRecord from log or another node
    */
  def add(qsoRecord: QsoRecord): Unit = {
    contacts.putIfAbsent(qsoRecord.uuid, qsoRecord)
  }
val jpuirnalDir = Paths.get("fdlog/")
  Files.createDirectories(jpuirnalDir)
  private val path: Path = jpuirnalDir.resolve("journal.log")
  private val outputStream: OutputStream = Files.newOutputStream(path, StandardOpenOption.APPEND , StandardOpenOption.CREATE)

  def writeJournal(jsValue: JsValue) = {
    val lineOfJson = jsValue.toString()

    outputStream.write(lineOfJson.getBytes())
    outputStream.write("\n".getBytes())
    outputStream.flush()
  }

  /**
    * Add this qso if not a dup.
    *
    * @param potentialContact that may be added.
    * @return None if added, otherwise [[Contact]] that this is a dup of.
    */
  override def add(potentialContact: Qso)(implicit stationContext: StationContext): Option[QsoRecord] = {
    findDup(potentialContact) match {
      case dup@Some(_) =>
        dup
      case None =>
        val newRecord = QsoRecord(nodeInfo.contest, stationContext.station, potentialContact, nodeInfo.fdLogId)
        add(newRecord)
        import org.wa9nnn.fdlog.model.Contact._
        val jsValue = Json.toJson(newRecord)
        writeJournal(jsValue)
        None
    }
  }

  def findDup(potentialQso: Qso): Option[QsoRecord] = {
    for {
      contacts <- byCallsign.get(potentialQso.callsign)
      dup ← contacts.find(_.qso.station == potentialQso.station)
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