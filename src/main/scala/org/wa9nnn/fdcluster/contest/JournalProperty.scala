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

import org.wa9nnn.util.Persistence
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.{ContestProperty, NodeAddress}
import scalafx.beans.property.ObjectProperty
import org.wa9nnn.fdcluster.model.MessageFormats._
import javax.inject.{Inject, Singleton}

@Singleton
class JournalProperty @Inject()(persistence: Persistence, contestProperty: ContestProperty, nodeAddress: NodeAddress)
  extends ObjectProperty[Option[Journal]]
    with LazyLogging {

  def newJournal(): Unit = {
    val contest = contestProperty.contest
    contest.checkValid
    val id = contest.id
    value = Some(Journal(id, nodeAddress))
  }

  def maybeJournal: Option[Journal] = value

  def fileName: String = value.map(_.journalFileName).getOrElse("File not set!")

  value = persistence.loadFromFile[JournalContainer](() => JournalContainer()).maybeJournal

  onChange { (_, _, nv) =>
    persistence.saveToFile(nv)
  }

}

case class JournalContainer(maybeJournal: Option[Journal] = None)
