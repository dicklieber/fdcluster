/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdcluster.store

import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.javafx.sync.SyncSteps
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{NodeStatus, QsoHour}
import org.wa9nnn.fdcluster.store
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.util.JsonLogging
import play.api.libs.json.{JsValue, Json}
import scalafx.collections.ObservableBuffer

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import java.security.{MessageDigest, SecureRandom}
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.util.Using

/**
 * This can only be used within the [[StoreActor]]
 *
 * @param nodeInfo                    who we are
 * @param ourStationStore             things that may vary with operator.
 * @param journalFilePath             where journal file lives.
 */
class StoreMapImpl(nodeInfo: NodeInfo,
                   ourStationStore: OurStationStore,
                   bandModeStore: BandModeOperatorStore,
                   allQsos: ObservableBuffer[QsoRecord],
                   syncSteps: SyncSteps = new SyncSteps,
                   val journalFilePath: Option[Path] = None)
  extends Store with JsonLogging with DefaultInstrumented {
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

  private def displayProgress(count: Int)(implicit start: Instant): Unit = {
    if (count > 0 && count % 250 == 0) {
      val seconds = Duration.between(start, Instant.now()).getSeconds
      if (seconds > 0) {
        val qsoPerSecond = count / seconds
        logger.info(f"loaded $count%,d records. ($qsoPerSecond%,d)/per sec")
      }
    }
  }

  private val outputStream: Option[OutputStream] = journalFilePath.map { path ⇒
    if (Files.exists(path)) {
      val count = new AtomicInteger()
      val lineNumber = new AtomicInteger()
      val errorCount = new AtomicInteger()
      implicit val start = Instant.now()
      Using(Source.fromFile(path.toUri)) { bufferedSource ⇒
        bufferedSource.getLines()
          .foreach { line: String ⇒
            lineNumber.incrementAndGet()
            try {
              val qsoRecord = Json.parse(line).as[QsoRecord]
              insertQsoRecord(qsoRecord)
              displayProgress(count.incrementAndGet())

            } catch {
              case e: Exception =>
                val err = errorCount.incrementAndGet()
                err match {
                  case 25 =>
                    logger.error("More than 25 errors, stopping logging!")
                  case x if x < 25 =>
                    logger.error(f"loading QSO from line ${lineNumber.get()}%,d")
                }
            }
          }
      }
      if (errorCount.get > 0) {
        logger.info(f"${errorCount.get}%,d lines with errors in $path")
      }
      val seconds = Duration.between(start, Instant.now()).toMillis * 1000.0
      val c: Int = count.get()
      val qsoPerSecond: Double = c / seconds
      logger.info(f"loaded $c%,d records. ($qsoPerSecond%.2f/per sec)")
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
        val newRecord = QsoRecord(potentialQso, nodeInfo.contest, ourStationStore.value, nodeInfo.fdLogId)
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

  override def dump: QsosFromNode = QsosFromNode(nodeInfo.nodeAddress, byUuid.values.toList.sorted)

  /**
   *
   * @return ids of all [[NodeDatabase]] known to this node.
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
    syncSteps.step("Considering", qsoRecords.size)

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
      syncSteps.finish("Merge Done", s"Merged: $mergeCount Already: $existedCount")
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
    sync.NodeStatus(nodeInfo.nodeAddress, nodeInfo.url, byUuid.size, sDigest, hourDigests, ourStationStore.value, bandModeStore.bandModeOperator, rate)
  }

  /**
   *
   * @param fdHours [[List.empty]] returns all Uuids for all QSPOs.
   */
  override def uuidForHours(fdHours: Set[FdHour]): List[Uuid] = {
    if (fdHours.isEmpty) {
      byUuid.keys.toList
    } else {
      allQsos.flatMap { qr ⇒
        if (fdHours.contains(qr.fdHour)) {
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

