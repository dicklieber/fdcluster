/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.security.MessageDigest
import java.time.{Duration, Instant}

import nl.grons.metrics.scala.DefaultInstrumented
import org.wa9nnn.fdlog.javafx.sync.Step
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHour}
import org.wa9nnn.fdlog.store.network.FdHour
import org.wa9nnn.util.JsonLogging
import play.api.libs.json.{JsValue, Json}
import resource._
import scalafx.collections.ObservableBuffer

import scala.collection.concurrent.TrieMap
import scala.io.Source
import org.wa9nnn.fdlog.javafx.sync.StepsDataMethod.addStep

/**
 * This can only be used within the [[StoreActor]]
 *
 * @param nodeInfo                    who we are
 * @param currentStationProvider      things that may vary with operator.
 * @param journalFilePath             where journal file lives.
 */
class StoreMapImpl(nodeInfo: NodeInfo,
                   currentStationProvider: CurrentStationProvider,
                   stepsData: ObservableBuffer[Step] = ObservableBuffer[Step](Seq.empty),
                   journalFilePath: Option[Path] = None)
  extends Store with JsonLogging with DefaultInstrumented {

  implicit val node: NodeAddress = nodeInfo.nodeAddress
  private val contacts = new TrieMap[Uuid, QsoRecord]()
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]

  def length: Int = contacts.size

  private val qsoMeter = metrics.meter("qso")
  private val qsosDigestTimer = metrics.timer("qsos digest")
  private val hourDigestsTimer = metrics.timer("hours digest")

  /**
   * for sync
   *
   * @param qsoRecord from log or another node
   */
  def addRecord(qsoRecord: QsoRecord): AddResult = {
    insertQsoRecord(qsoRecord)
    val jsValue = Json.toJson(qsoRecord)
    writeJournal(jsValue)
    Added(qsoRecord)

  }

  private def insertQsoRecord(qsoRecord: QsoRecord) = {
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
            insertQsoRecord(qsoRecord)
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
        qsoMeter.mark()
        addRecord(newRecord)
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
  def contactIds: NodeQsoIds = {
    NodeQsoIds(contacts.keys.toSet)
  }

  def requestContacts(contactRequest: ContactRequest): NodeDatabase = {

    val selectedContacts = if (contactRequest.contactIds.isEmpty) {
      contacts.values
    } else {
      contactRequest.contactIds.flatMap(contacts.get)
    }
    NodeDatabase(selectedContacts.toSeq)
  }

  def merge(qsoRecords: Seq[QsoRecord]): Unit = {
    stepsData.step("Considering", qsoRecords.size)

    try {
      var mergeCount = 0
      var existedCount = 0
      qsoRecords.foreach { qsoRecord ⇒ {
        addRecord(qsoRecord) match {
          case Added(qsoRecord) ⇒
            mergeCount = mergeCount + 1
          case Dup(qsoRecord) ⇒
            existedCount = existedCount + 1
        }
      }
      }
      stepsData.step("Merge Done", s"Merged: $mergeCount Already: $existedCount")
    } catch {
      case eT: Throwable ⇒
        logger.error("merge", eT)

    }
  }

  override def size: Int = contacts.size

  override def nodeStatus: NodeStatus = {
    //todo Needs some serious caching. Past hours don't usually change (unless syncing)

    val sDigest = qsosDigestTimer.time {
      val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
      contacts.values.foreach(qr ⇒ messageDigest.update(qr.fdLogId.uuid.getBytes()))
      val bytes = messageDigest.digest()
      val encoder = java.util.Base64.getEncoder
      val bytes1 = encoder.encode(bytes)
      new String(bytes1)
    }

    val hourDigests = hourDigestsTimer.time {
      contacts
        .values
        .toList
        .groupBy(_.fdHour)
        .values.map(QsoHour(_))
        .map(_.hourDigest).toList
        .sortBy(_.startOfHour)
    }
    val rate = qsoMeter.fifteenMinuteRate
    NodeStatus(nodeInfo.nodeAddress, nodeInfo.url, contacts.size, sDigest, hourDigests, currentStationProvider.currentStation, rate)
  }


  def get(fdHour: FdHour): List[QsoHour] = {
    contacts.values
      .toList
      .sorted.groupBy(_.fdHour).values.map(QsoHour(_))
      .filter(_.startOfHour == fdHour)
      .toList
  }

  override def debugClear(): Unit = {
    logger.info(s"Clearing this nodes store for debugging!")
    contacts.clear()
    byCallsign.clear()
  }
}