package org.wa9nnn.fdcluster.model

import com.wa9nnn.util.TimeConverters.fileStamp

import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

/**
 * Holds the journal filename along with some metadata about when and where is was set.
 *
 * @param journalFileName     name of file contestname+YYYMMddHHmmssz.json.
 * @param nodeAddress         who started the instance.
 * @param stamp               when this was created. Newer always replaces older, anywhere in the cluster.
 */
case class Journal(journalFileName: String = "", nodeAddress: NodeAddress = NodeAddress(), stamp: Instant = Instant.EPOCH)
  extends Stamped[Journal] {
  def check(): Unit = if (!isValid) throw new IllegalStateException("Journal Not initialized!")

  def isValid: Boolean = journalFileName.nonEmpty
}

object Journal {
  private val fileStamp = DateTimeFormatter.ofPattern("YYMMdd")


  def newJournal(contestName: String, nodeAddress: NodeAddress, instant: Instant = Instant.now()): Journal = {
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
    val localDate = localDateTime.toLocalDate
    val localTime = localDateTime.toLocalTime
    val fileName = s"$contestName${localDate.getYear}${localDate.getMonthValue}${localDate.getDayOfMonth}.${localTime.toSecondOfDay}.json"
//    val fileName = contestName + str + ".json"
    new Journal(fileName, nodeAddress, instant)
  }

  def apply(): Journal = new Journal()
}


/**
 * JSON of this is the 1st line in the journal.
 *
 * @param journal        as sent around the cluster.
 * @param ourNodeAddress so we can tell whose journal this is.
 */
case class JournalHeader(journal: Journal, ourNodeAddress: NodeAddress)