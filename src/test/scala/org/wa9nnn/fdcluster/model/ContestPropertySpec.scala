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
import org.wa9nnn.fdcluster.{FileContext, MockFileContext}
import org.wa9nnn.util.PersistenceImpl

trait FileManagerContext extends ForEach[FileContext] {
  private val fileManager = MockFileContext()

  override protected def foreach[R](f: FileContext => R)(implicit evidence$3: AsResult[R]): Result = {
    try AsResult(f(fileManager))
    finally fileManager.clean()
  }

}

class ContestPropertySpec extends Specification with FileManagerContext {
  sequential
  "ContestProperty" should {
   "exchange properties" >> { fileContext: FileContext =>
      val persistence = new PersistenceImpl(fileContext)
      val contestProperty = new ContestProperty(fileContext, NodeAddress())
      val ourExchangeProperty = contestProperty.ourExchangeProperty
      val ourExchange = ourExchangeProperty.value
      ourExchange.display must beEqualTo ("1O AB")

      ourExchangeProperty.value = ourExchange.copy(sectionCode = "DX")
      ourExchange.display must beEqualTo ("1O AB")
    }
  }

}
