
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.FieldValidator
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.{StructuredLogging, WithDisposition}
import scalafx.beans.binding.{Bindings, BooleanBinding}
import scalafx.beans.property.BooleanProperty
import scalafx.scene.control.TextInputControl

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