
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.util.InputHelper.forceCaps
import scalafx.beans.property.BooleanProperty
import scalafx.scene.control.TextInputControl

trait NextField extends TextInputControl {
  forceCaps(this)

  var onDoneFunction: Char => Unit = (_: Char) => {}

  def onDone(f: Char => Unit): Unit = {
    onDoneFunction = f
  }

  val validProperty:BooleanProperty  = new BooleanProperty()
  validProperty.value = false

  def reset(): Unit = {
    text = ""
  }
}