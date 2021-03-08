
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.beans.binding.{Bindings, BooleanBinding}
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField extends TextField with WithDisposition with NextField {

  onKeyTyped = { event: KeyEvent =>
    event.character.headOption match {
      case Some(char) =>
        // If we're a valid call sign and the next char is digit we want move to the next field.
        if (char.isDigit && CallsignValidator.valid(text.value).isEmpty) {
          text = text.value.dropRight(1)
          onDoneFunction(char)
        }
      case None =>
        // Not a Char.
    }
    // this validates twice if char was a digit, but not much of an inefficiency.
    CallsignValidator.valid(text.value) match {
      case Some(_: String) =>
        sad()
      case None =>
        happy()
    }
  }
  val b: BooleanBinding = Bindings.createBooleanBinding(
    () => {
      val str = Option(text.value).getOrElse("")
      CallsignValidator.valid(str).isEmpty
    }
    ,
    text
  )
  validProperty.bind(b)

}

