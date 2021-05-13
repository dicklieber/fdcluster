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
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.JournalManager.notSet
import org.wa9nnn.fdcluster.model.ContestProperty
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.store.{ClearStore, StoreSender}
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

import java.io.IOException
import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.language.implicitConversions
import scala.util.Try

@Singleton
class JournalManager @Inject()(
                                fileManager: FileContext,
                                contestProperty: ContestProperty,
                                storeSender: StoreSender
                              )
  extends OkToLogContributer with   LazyLogging {
  var _currentJournal: Option[Journal] = None

  val journalFilePathProperty: ObjectProperty[Try[Path]] = ObjectProperty[Try[Path]] {
    Try {
      val journal: Journal = fileManager.loadFromFile[Journal](() => throw new IllegalStateException())
      _currentJournal = Some(journal)
      journalPath(journal).get
    }
  }

  val okToLogProperty:BooleanProperty = new BooleanProperty(){
   value = journalFilePathProperty.value.isSuccess
    journalFilePathProperty.onChange { (_, _, nv) =>
      value = nv.isSuccess
    }
  }
  private def journalPath(journal: Journal): Try[Path] = {
    Try {
      Files.createDirectories(fileManager.journalDir)
      fileManager.journalDir.resolve(journal.journalFileName)
    }
  }

  /**
   * new later journal.
   */
  def createNewJournal(): Unit = {
    val contest = contestProperty.contest
    contest.checkValid()
    val id = contest.id
    val journal = Journal(id, fileManager.nodeAddress)
    updateJournal(journal)
  }

  /**
   *
   * @param candidate from createNewJournal or another node
   */
  def updateJournal(candidate: Journal): Unit = {
    if (_currentJournal.isEmpty || _currentJournal.exists(current => candidate.stamp.isAfter(current.stamp))) {
      _currentJournal = Option(candidate)
      fileManager.saveToFile(candidate)
      journalFilePathProperty.value = journalPath(candidate)
      storeSender ! ClearStore
    }
  }
}

object JournalManager {
  val notSet: String = "Not Set"

  implicit def tp2String(tryPath: Try[Path]): String = {
    tryPath.map(_.getFileName.toString).getOrElse(notSet)

  }
}

class NoJournalDefined extends Exception(notSet)

class JournalNotWritable(journal: Path) extends IOException(s"$journal is not writeable!")
