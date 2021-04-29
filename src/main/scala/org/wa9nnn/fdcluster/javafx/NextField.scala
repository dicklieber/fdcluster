
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

import org.wa9nnn.fdcluster.javafx.entry.FieldValidator
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.{StructuredLogging, WithDisposition}
import _root_.scalafx.beans.binding.{Bindings, BooleanBinding}
import _root_.scalafx.beans.property.BooleanProperty
import _root_.scalafx.scene.control.TextInputControl

/**
 * Most of the common logic for any qso input field.
 */
trait NextField extends TextInputControl with WithDisposition with StructuredLogging {
  forceCaps(this)
  styleClass += "qsoField"
  sad()

  var onDoneFunction: String => Unit = (_: String) => {}

  def onDone(f: String => Unit): Unit = {
    onDoneFunction = f
  }

  val validProperty: BooleanProperty = new BooleanProperty()
  validProperty.value = false

  if (logger.isTraceEnabled()) {
    validProperty.onChange((_, _, nv) =>
      logger.trace(s"valid: $nv")
    )

    text.onChange((_, _, nv) =>
      logger.trace(s"text: $nv")
    )
  }

  validProperty.onChange{(_,_,nv) =>
    disposition(nv)
  }

  def reset(): Unit = {
    text = ""
  }

  /**
   * @deprecated handle within the control. Manipulate [[validProperty]]
   * @param fieldValidator
   */
  def setFieldValidator(fieldValidator: FieldValidator) {
    val b: BooleanBinding = Bindings.createBooleanBinding(
      () => {
        fieldValidator.valid(text).isEmpty
      }
      ,
      text
    )
    validProperty.bind(b)
  }
}