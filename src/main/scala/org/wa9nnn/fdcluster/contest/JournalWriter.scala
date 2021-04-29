/*
 * Copyright © 2021 Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdcluster.contest

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{NodeAddress, QsoRecord}
import play.api.libs.json.Json

import java.io.IOException
import java.nio.file.{Files, StandardOpenOption}
import javax.inject.{Inject, Singleton}

/**
 * Handles access and naming of the Qso journal.
 *
 * @param journalProperty updated from any node and synced via NodeStatus messages.
 * @param nodeAddress     us.
 */
@Singleton
class JournalWriter @Inject()(val journalProperty: JournalPropertyWriting, nodeAddress: NodeAddress) extends LazyLogging {
  /**
   *  creating the file with header as needed.
   * @param qsoRecord of interest.
   */
  def write(qsoRecord: QsoRecord): Unit = {
    journalProperty.maybeJournal.foreach { journal =>
      val filePath = journalProperty.filePath
      if (!Files.exists(filePath) || Files.size(filePath) == 0) {
        try {
          val json = Json.toJson(JournalHeader(journal, nodeAddress)).toString()
          Files.writeString(filePath, json + "\n", StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        } catch {
          case e: IOException =>
            logger.error(s"Writing header to $filePath", e)
        }
      }
      val lineOfJson = Json.toJson(qsoRecord).toString() + "\n"
      try {
        Files.writeString(filePath, lineOfJson, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
      } catch {
        case e: IOException =>
          logger.error(s"Writing QSO: $lineOfJson to $filePath", e)
      }
    }
  }
}
