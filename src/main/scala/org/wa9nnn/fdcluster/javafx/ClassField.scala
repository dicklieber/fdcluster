
package org.wa9nnn.fdcluster.javafx


import org.wa9nnn.fdcluster.javafx.entry.ContestClass
import org.wa9nnn.util.InputHelper.forceCaps
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
class ClassField extends TextField with WithDisposition with NextField {
  forceCaps(this)
  var previousWasValid = false
  onKeyTyped = { event: KeyEvent =>
    if (previousWasValid) {
      onDoneFunction(event.character.head)
      text = text.value.dropRight(1)
    }
    previousWasValid = validProperty.value
  }

  val b: BooleanBinding = Bindings.createBooleanBinding(
    () => {
      val str = Option(text.value).getOrElse("")
      ContestClass.valid(str).isEmpty
    }
    ,
    text
  )
  validProperty.bind(b)

}

