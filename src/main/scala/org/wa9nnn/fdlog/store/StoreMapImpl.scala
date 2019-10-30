/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.security.{MessageDigest, SecureRandom}
import java.time.{Duration, Instant}

import nl.grons.metrics.scala.DefaultInstrumented
import org.wa9nnn.fdlog.javafx.sync.Step
import org.wa9nnn.fdlog.javafx.sync.StepsDataMethod.addStep
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

/**
 * This can only be used within the [[StoreActor]]
 *
 * @param nodeInfo                    who we are
 * @param currentStationProvider      things that may vary with operator.
 * @param journalFilePath             where journal file lives.
 */
class StoreMapImpl(nodeInfo: NodeInfo,
                   currentStationProvider: CurrentStationProvider,
                   allQsos: ObservableBuffer[QsoRecord],
                   stepsData: ObservableBuffer[Step] = ObservableBuffer[Step](Seq.empty),
                   val journalFilePath: Option[Path] = None)
  extends Store with JsonLogging with DefaultInstrumented {

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
      byUuid.remove(targetQsoRecord.uuid)
    }
  }


  implicit val node: NodeAddress = nodeInfo.nodeAddress
  private val byCallsign = new TrieMap[CallSign, Set[QsoRecord]]
  /**
   * This is the canonical set of data. If its not here then we know we have to also add to [[byCallsign]] and [[allQsos]]
   */
  private val byUuid = new TrieMap[Uuid, QsoRecord]()

  def length: Int = byUuid.size

  private val qsoMeter = metrics.meter("qso")
  private val qsosDigestTimer = metrics.timer("qsos digest")
  private val hourDigestsTimer = metrics.timer("hours digest")

  /**
   * This will also add to journal on disk; unless duplicate.
   *
   * @param qsoRecord from log or another node
   * @return [[Dup]] is already in this node. [[Added]] if new to this node.
   */
  def addRecord(qsoRecord: QsoRecord): AddResult = {
    insertQsoRecord(qsoRecord) match {
      case Some(_) ⇒
        Dup(qsoRecord)
      case None ⇒
        val jsValue = Json.toJson(qsoRecord)
        writeJournal(jsValue)
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
    val maybeExisting = byUuid.putIfAbsent(qsoRecord.uuid, qsoRecord)
    if (maybeExisting.isEmpty) {
      allQsos.add(qsoRecord)
      val callsign = qsoRecord.qso.callsign
      val qsoRecords: Set[QsoRecord] = byCallsign.getOrElse(callsign, Set.empty) + qsoRecord
      byCallsign.put(callsign, qsoRecords)
    }
    maybeExisting
  }

  private val outputStream: Option[OutputStream] = journalFilePath.map { path ⇒

    if (Files.exists(path)) {
      var count = 0
      val start = Instant.now()
      managed(Source.fromFile(path.toUri)) acquireAndGet { bufferedSource ⇒
        bufferedSource.getLines()
          .foreach { line: String ⇒

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
    byUuid.values.find(_.qso.callsign.contains(in))
    }.toSeq

  override def dump: QsosFromNode = QsosFromNode(nodeInfo.nodeAddress,  byUuid.values.toList.sorted)

  /**
   *
   * @return ids of all [[NodeDatabase]] known to this node.
   */
  def contactIds: NodeQsoIds = {
    NodeQsoIds(byUuid.keys.toSet)
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
    stepsData.step("Considering", qsoRecords.size)

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
      stepsData.step("Merge Done", s"Merged: $mergeCount Already: $existedCount")
    } catch {
      case eT: Throwable ⇒
        logger.error("merge", eT)

    }
  }

  override def size: Int = byUuid.size

  override def nodeStatus: NodeStatus = {
    //todo Needs some serious caching. Past hours don't usually change (unless syncing)

    val sDigest = qsosDigestTimer.time {
      val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
      byUuid.values.foreach(qr ⇒ messageDigest.update(qr.fdLogId.uuid.getBytes()))
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
        .sortBy(_.startOfHour)
    }
    val rate = qsoMeter.fifteenMinuteRate
    NodeStatus(nodeInfo.nodeAddress, nodeInfo.url, byUuid.size, sDigest, hourDigests, currentStationProvider.currentStation, rate)
  }

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  override def uuidForHours(fdHours: Set[FdHour]): List[Uuid] = {
    if (fdHours.isEmpty) {
      byUuid.keys.toList
    } else {
      val fdHourSet = fdHours.toSet
      allQsos.flatMap { qr ⇒
        if (fdHourSet.contains(qr.fdHour)) {
          Seq(qr.uuid)
        } else {
          Seq.empty
        }
      }
      }.toList
  }


  def get(fdHour: FdHour): List[QsoHour] = {
    byUuid.values
      .toList
      .sorted.groupBy(_.fdHour).values.map(QsoHour(_))
      .filter(_.startOfHour == fdHour)
      .toList
  }

  override def debugClear(): Unit

  = {
    logger.info(s"Clearing this nodes store for debugging!")
    byUuid.clear()
    byCallsign.clear()
    allQsos.clear()
  }

  override def missingUuids(uuidsAtOtherHost: List[Uuid]): List[Uuid] = {
    uuidsAtOtherHost.filter(otherUuid ⇒
      !byUuid.contains(otherUuid)
    )
  }
}

