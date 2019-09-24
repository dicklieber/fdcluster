/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.{Duration, Instant}

import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics.scala.DefaultInstrumented
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHour}
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.{JsValue, Json}
import resource._

import scala.collection.concurrent.TrieMap
import scala.io.Source

/**
 * This can only be used within the [[StoreActor]]
 * @param nodeInfo                    who we are
 * @param currentStationProvider      things that may vary with operator.
 * @param journalFilePath             where journal file lives.
 */
class StoreMapImpl(nodeInfo: NodeInfo, currentStationProvider: CurrentStationProvider, journalFilePath: Option[Path] = None)
  extends Store with LazyLogging  with DefaultInstrumented {

  implicit val node: NodeAddress = nodeInfo.nodeAddress
  private val contacts = new TrieMap[Uuid, QsoRecord]()
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]

  def length: Int = contacts.size
  private val qsoMeter = metrics.meter("qso")
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

    if (Files.exists(path)) {
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
   * @return None if added, otherwise [[org.wa9nnn.fdlog.model.MessageFormats]] that this is a dup of.
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
        qsoMeter.mark()
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

  //  def merge(contactFromAnotherNode: NodeDatabase): Unit = {
  //
  //    contactFromAnotherNode.records.foreach {
  //      contact ⇒ {
  //        //      val maybeExisting = contacts.putIfAbsent(contact.uuid, contact)
  //        //      if (logger.isDebugEnabled) {
  //        //        (maybeExisting match {
  //        //          case None ⇒
  //        //            logJson("merged")
  //        //          case Some(_) ⇒
  //        //            logJson("exists")
  //        //
  //        //        })
  //        //          .field("uuid", contact.uuid)
  //        //          .field("worked", contact.callsign)
  //        //          .debug()
  //        //      }
  //      }
  //    }
  //  }

  override def size: Int = contacts.size

  override def nodeStatus: NodeStatus = {
    //todo Needs some serious caching. Past hours don't usually change (unless syncing)
    val hourDigests = contacts.values
      .toList
      .groupBy(_.fdHour)
      .values.map(QsoHour(_))
      .map(_.hourDigest).toList
      .sortBy(_.startOfHour)
    qsoMeter.oneMinuteRate
    NodeStatus(nodeInfo.nodeAddress, nodeInfo.url, contacts.size, hourDigests, currentStationProvider.currentStation)
  }


  def get(fdHour: FdHour):  List[QsoHour] = {
    contacts.values
      .toList
      .sorted.groupBy(_.fdHour).values.map(QsoHour(_))
      .filter(_.startOfHour == fdHour)
      .toList
  }

}