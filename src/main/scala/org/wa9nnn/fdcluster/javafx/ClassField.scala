
package org.wa9nnn.fdcluster.javafx


import org.wa9nnn.fdcluster.javafx.entry.ContestClassValidator
import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class ClassField extends TextField with WithDisposition with NextField {
  setFieldValidator(ContestClassValidator)

  onKeyTyped = { event: KeyEvent =>
    val sChar = event.character
    val character = sChar.headOption.getOrElse(Char.MinValue)
    if (validProperty.value) {
      event.consume()
      onDoneFunction(sChar)
    }
  }
}

