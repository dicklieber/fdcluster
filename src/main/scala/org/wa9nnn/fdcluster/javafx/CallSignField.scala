
package org.wa9nnn.fdcluster.javafx

import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField extends TextField with NextField {
  setFieldValidator(CallsignValidator)

   onKeyTyped = { event: KeyEvent =>

    val sChar = event.character
    val character = sChar.headOption.getOrElse(Char.MinValue)
    if (character.isDigit && validProperty.value) {
      event.consume()
      onDoneFunction(sChar)
    }
  }
}

