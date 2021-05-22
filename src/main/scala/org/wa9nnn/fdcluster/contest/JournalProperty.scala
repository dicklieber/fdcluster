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

import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.JournalProperty.notSet
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{ContestProperty, Journal, PersistableProperty}
import org.wa9nnn.fdcluster.store.{ClearStore, StoreSender}
import scalafx.beans.property.ObjectProperty

import java.io.IOException
import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.language.implicitConversions
import scala.util.{Failure, Try}

@Singleton
class JournalProperty @Inject()(
                                 fileContext: FileContext,
                                 contestProperty: ContestProperty,
                                 storeSender: StoreSender
                               )
  extends PersistableProperty[Journal](fileContext) {

  lazy val journalFilePathProperty: ObjectProperty[Try[Path]] = ObjectProperty[Try[Path]](Failure(new IllegalStateException()))


  /**
   * provide a new default instance of T. Needed when there is no file persisted/
   *
   * @return
   */
  override def defaultInstance: Journal = Journal()

  /**
   * Invoked initially and when the property changes.
   */
  override def onChanged(journal: Journal): Unit = {
    journalFilePathProperty.value = Try {
      Files.createDirectories(fileContext.journalDir)
      journal.check
      fileContext.journalDir.resolve(journal.journalFileName)
    }

    okToLogProperty.value = journalFilePathProperty.value.isSuccess
  }

  okToLogProperty.value = journalFilePathProperty.value.isSuccess

  /**
   * new later journal.
   */
  def createNewJournal(): Unit = {
    val contest = contestProperty.contest
    contest.checkValid()
    val id = contest.id
    val journal = Journal(id, fileContext.nodeAddress)
    update(journal)
    storeSender ! ClearStore

  }

}

object JournalProperty {
  val notSet: String = "Not Set"

  implicit def tp2String(tryPath: Try[Path]): String = {
    tryPath.map(_.getFileName.toString).getOrElse(notSet)

  }
}

class NoJournalDefined extends Exception(notSet)

class JournalNotWritable(journal: Path) extends IOException(s"$journal is not writeable!")
