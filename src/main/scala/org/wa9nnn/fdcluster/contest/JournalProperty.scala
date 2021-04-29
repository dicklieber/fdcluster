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
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{ContestProperty, NodeAddress}
import org.wa9nnn.util.Persistence
import scalafx.beans.property.ObjectProperty

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

@Singleton
class JournalProperty @Inject()(persistence: Persistence, fileManager: FileManager, contestProperty: ContestProperty, nodeAddress: NodeAddress)
  extends ObjectProperty[Journal] with LazyLogging with JournalPropertyWriting {

  def filePath: Path = {
    fileManager.journalDir.resolve(value.journalFileName)
  }

  def fileName:String = {
    Option(value) match {
      case Some(value) => value.journalFileName
      case None =>"Not Set"
    }
  }


  try {
    val journal: Journal = persistence.loadFromFile[Journal](() => throw new IllegalStateException())
    value = journal
  } catch {
    case _: Exception =>
      // leaving value to be null
      logger.debug("No persisted Journal")
  }

  def maybeJournal: Option[Journal] = Option(value)

  def hasJournal: Boolean = maybeJournal.isDefined

  def newJournal(): Unit = {
    val contest = contestProperty.contest
    contest.checkValid
    val id = contest.id
    val newJournal = Journal(id, nodeAddress)
    persistence.saveToFile(newJournal)
    value = newJournal
  }
}

trait JournalPropertyWriting {
  def maybeJournal: Option[Journal]
  def filePath: Path
}



