
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.ActionResult
import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.{KeyCode, KeyEvent}

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField(actionResult: ActionResult) extends TextField with NextField {

  setFieldValidator(CallsignValidator)
  text.onChange { (_, _, nv) =>
    actionResult.clear()
    if (!validProperty.value) {
      if (text.value.isEmpty) {

      } else {
        actionResult.potentiaDup(nv)
      }
    }
  }

  onKeyPressed = { event: KeyEvent =>
    val key: KeyCode = event.code
    if (key.isDigitKey && validProperty.value) {
      event.consume()
      val str: String = key.name
      onDoneFunction(str)
    }
  }
}

