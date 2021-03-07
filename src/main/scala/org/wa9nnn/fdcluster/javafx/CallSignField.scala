
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField extends TextField with WithDisposition with NextField {
  forceCaps(this)


  onKeyTyped = { event: KeyEvent =>
    event.character.headOption match {
      case Some(char) =>
        // If we're a valid call sign and the next char is digit we want move to the next field.
        if (char.isDigit && CallsignValidator.valid(text.value).isEmpty)
          onDoneFunction(char)
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

  private var onDoneFunction: Char => Unit = (_: Char) => {}

  override def onDone(f: Char => Unit): Unit = {
    onDoneFunction= f
  }
}

trait NextField {
  def onDone(f: Char => Unit): Unit
}