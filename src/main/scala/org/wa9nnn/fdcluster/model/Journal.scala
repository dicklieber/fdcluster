package org.wa9nnn.fdcluster.model

import com.wa9nnn.util.TimeConverters.fileStamp

import java.time.Instant

/**
 * Holds the journal filename along with some metadata about when and where is was set.
 * @param journalFileName     name of file contestname+YYYMMddHHmmssz.json, obtained from [[com.wa9nnn.util.TimeConverters#fileStamp(java.time.Instant]])
 * @param nodeAddress         who started the instance.
 * @param stamp               when this was created. Newer always replaces older, anywhere in the cluster.
 */
case class Journal (journalFileName: String = "", nodeAddress: NodeAddress = NodeAddress(), stamp: Instant = Instant.EPOCH)
  extends Stamped[Journal]{
  def check(): Unit = if(!isValid) throw new IllegalStateException("Journal Not initialized!")

  def isValid:Boolean = journalFileName.nonEmpty

}

object Journal {
  def apply(contestName: String, nodeAddress: NodeAddress): Journal = {
    val instant = Instant.now()
    val fileName = contestName + fileStamp(instant) + ".json"
    new Journal(fileName, nodeAddress, instant)
  }

  def apply():Journal = new Journal()
}


/**
 * JSON of this is the 1st line in the journal.
 *
 * @param journal        as sent around the cluster.
 * @param ourNodeAddress so we can tell whose journal this is.
 */
case class JournalHeader(journal: Journal, ourNodeAddress: NodeAddress)