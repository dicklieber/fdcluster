
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

package org.wa9nnn.fdcluster.javafx.entry.section

import org.wa9nnn.fdcluster.javafx.NextField
import org.wa9nnn.fdcluster.javafx.entry.ContestSectionValidator
import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Section entry field for entring QSOs
 * sad or happy as validated while typing.
 *
 */
class SectionField() extends TextField with WithDisposition with NextField {
  setFieldValidator(ContestSectionValidator)
   val sectionPrompt = new SectionPrompt()(this)


  onKeyTyped = { event: KeyEvent =>
    val ch = event.character.headOption.getOrElse("")

    if (ch == '\r' && validProperty.value) {
      onDoneFunction("") // noting to pass on, were at the end of the QSO
    }
  }

  override def onDone(f: String => Unit): Unit = super.onDone(f)

  override def reset(): Unit = {
    super.reset()
  }
}

