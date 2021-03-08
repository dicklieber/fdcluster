
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.util.WithDisposition
import scalafx.Includes._
import scalafx.beans.binding.{Bindings, BooleanBinding}
import scalafx.scene.control.{TextArea, TextField}
import scalafx.scene.input.KeyEvent

/**
 * Section entry field
 * sad or happy as validated while typing.
 *
 */
class SectionField(sectionPrompt: TextArea) extends TextField with WithDisposition with NextField {
  private val allSections = Sections.sections.map { section: Section ⇒ f"${section.code}%-3s" }
    .grouped(7)
    .map(_.mkString(" "))
    .mkString("\n")

  sectionPrompt.setText(allSections)

  text.onChange { (_, _, newValue) =>
    sectionPrompt.setText(Sections
      .find(newValue)
      .map(section ⇒ section.code + ": " + section.name)
      .mkString("\n")
    )
  }

  onKeyTyped = { event: KeyEvent =>
    val ch = event.character.head
    if(ch == '\r' && validProperty.value) {
      onDoneFunction(ch)
    }
    }

  override def onDone(f: Char => Unit): Unit = super.onDone(f)

  val b: BooleanBinding = Bindings.createBooleanBinding(
    () => {
      val str = Option(text.value).getOrElse("")
      val value = Sections.find(str)
      value.size <= 3
    }, text
  )
  validProperty.bind(b)

  override def reset(): Unit = {
    super.reset()
    sectionPrompt.setText(allSections)
  }
}

