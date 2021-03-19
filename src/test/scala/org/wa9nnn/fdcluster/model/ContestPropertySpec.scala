/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
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
 *
 */

package org.wa9nnn.fdcluster.model

import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.{FileManager, MockFileManager}
import org.wa9nnn.util.{Persistence, PersistenceImpl}
import scalafx.beans.property.{IntegerProperty, StringProperty}

trait FileManagerContext extends ForEach[FileManager] {
  private val fileManager = MockFileManager()

  override protected def foreach[R](f: FileManager => R)(implicit evidence$3: AsResult[R]): Result = {
    try AsResult(f(fileManager))
    finally fileManager.clean()
  }

}

class ContestPropertySpec extends Specification with FileManagerContext {
  "ContestProperty" should {
    "propMap" >> { fileManger: FileManager =>
      val persistence = new PersistenceImpl(fileManger)
      val contestProperty = new ContestProperty(persistence)
      val was: Contest = persistence.loadFromFile[Contest].get
      was.year must beEqualTo (2021)
      val eventyear: StringProperty = contestProperty.eventYearProperty
      eventyear.value must beEqualTo (2021) //todo mke this work net year too!
      eventyear.value = "1949"

      val value1 = contestProperty.b.value
      val newContest: Contest = persistence.loadFromFile[Contest].get


      newContest.year must beEqualTo (1949)

    }
  }
}
