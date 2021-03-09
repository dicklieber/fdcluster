
package org.wa9nnn.fdcluster.javafx

import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.{KeyCode, KeyEvent}

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField extends TextField with NextField {
  setFieldValidator(CallsignValidator)

   onKeyPressed = { event: KeyEvent =>

    val key: KeyCode = event.code
    if (key.isDigitKey && validProperty.value) {
      event.consume()
      val str: String = key.name
      onDoneFunction(str)
    }
  }
}

