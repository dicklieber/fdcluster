
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.util.InputHelper.forceCaps
import org.wa9nnn.util.WithDisposition
import scalafx.beans.binding.{Bindings, BooleanBinding}
import scalafx.scene.control.{TextArea, TextField}

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

  forceCaps(this)


  text.onChange { (_, _, newValue) =>

    sectionPrompt.setText(Sections
      .find(newValue)
      .map(section ⇒ section.code + ": " + section.name)
      .mkString("\n")
    )
  }
  val b: BooleanBinding = Bindings.createBooleanBinding(
    () => {
      val str = Option(text.value).getOrElse("")
      Sections.isValid(str)
    }
    ,
    text
  )
  validProperty.bind(b)

  override def reset(): Unit = {
    super.reset()
    sectionPrompt.setText(allSections)
  }

}

