
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

package org.wa9nnn.fdcluster.javafx


import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, ContestRules}
import org.wa9nnn.util.WithDisposition
import _root_.scalafx.scene.control.TextField

import javax.inject.{Inject, Singleton}
import scala.util.matching.Regex
/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
@Singleton
class ClassField @Inject()(allContestRules: AllContestRules, contestProperty: ContestProperty) extends TextField with WithDisposition with NextField {
  styleClass += "qsoClass"

  var entryCategories: ContestRules = _

  entryCategories = allContestRules.byContestName(contestProperty.contestName)
  contestProperty.contestNameProperty.onChange{ (_, _, contestName) =>
    entryCategories = allContestRules.byContestName(contestName)
  }

  text.onChange{(_,_,nv) =>
    try {
      val p(sTransmitters, designator) = nv
      validProperty.value = entryCategories.validDesignator(designator)
    } catch {
      case _:Throwable =>
        validProperty.value = false
    }
  }

  val p: Regex = """(\d+)([A-Z])""".r

  validProperty.onChange{(_,_,nv) =>
    if(nv) {
      // move to next field as soon as class is valid.
      onDoneFunction("")
    }
  }

}

