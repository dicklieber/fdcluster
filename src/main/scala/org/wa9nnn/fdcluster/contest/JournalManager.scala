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
import org.wa9nnn.fdcluster.FileManager
import org.wa9nnn.fdcluster.model.NodeAddress
import play.api.libs.json.{JsValue, Json}

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}
import javax.inject.{Inject, Singleton}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsoRecord

/**
 * Handles access and naming of the Qso journal.
 *
 * @param fileManager     to the FdCluster directory.
 * @param journalProperty updated from any node and synced via NodeStatus messages.
 */
@Singleton
class JournalManager @Inject()(fileManager: FileManager, val journalProperty: JournalProperty, nodeAddress: NodeAddress) extends LazyLogging {
  def hasJournal: Boolean = journalProperty.maybeJournal.isDefined

  def journalPath: Path = fileManager.journalDir.resolve(journalProperty.fileName)

  private var maybeOutputStream: Option[OutputStream] = None

  journalProperty.onChange { (_, was, nv: Option[Journal]) =>
    nv.foreach(openStream)
  }

  private def openStream(journal: Journal): Unit = {
    maybeOutputStream.foreach { os =>
      os.close()
      maybeOutputStream = None
    }
    if (Files.exists(journalPath)) {
      // already there, that fine.
    } else {
      //new write the header.
      val json = Json.toJson(JournalHeader(journal, nodeAddress)).toString()
      Files.writeString(journalPath, json)
    }
    maybeOutputStream = Some(Files.newOutputStream(journalPath, StandardOpenOption.APPEND, StandardOpenOption.CREATE))
  }

  private def write(jsValue: JsValue): Unit = {
    val outputStream: OutputStream = maybeOutputStream.orElse(throw new IllegalStateException("Contest log not started!")).get
    val lineOfJson = jsValue.toString()

    outputStream.write(lineOfJson.getBytes())
    outputStream.write("\n".getBytes())
    outputStream.flush()
  }

  def write(qsoRecord: QsoRecord): Unit = {
    write(Json.toJson(qsoRecord))
  }
}