
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.{ContestSectionValidator, Sections}
import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.scene.control.TextField
import scalafx.scene.input.KeyEvent

/**
 * Section entry field
 * sad or happy as validated while typing.
 *
 */
class SectionField() extends TextField with WithDisposition with NextField {
  setFieldValidator(ContestSectionValidator)
   val sectionPrompt = new SectionPrompt()(this)


  onKeyTyped = { event: KeyEvent =>
    val ch = event.character.headOption.getOrElse("")

    if (ch == '\r' && validProperty.value) {
      onDoneFunction("") // noting to pass on, were at the end of the QSO
    }
  }

  override def onDone(f: String => Unit): Unit = super.onDone(f)

  override def reset(): Unit = {
    super.reset()
  }
}

