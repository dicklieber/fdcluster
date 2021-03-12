
package org.wa9nnn.fdcluster.javafx.entry.section

import org.wa9nnn.fdcluster.javafx.entry.Sections
import scalafx.geometry.Orientation.Vertical
import scalafx.scene.control.{Button, ButtonBase}
import scalafx.scene.layout.{Pane, TilePane, VBox}

/**
 * A scalaFx pane that shows sections and allows mouse selection.
 * @param sectionField clicking a buitton will update this textfield.
 */
class SectionPrompt(implicit sectionField: SectionField) extends Pane {
  private val sectionButtons = Sections.sections.map {LongButton}
  private val sectionButtonsMap = sectionButtons.map(sb => sb.section -> sb).toMap

  private val allSections: TilePane = new TilePane() {
    orientation = Vertical
    prefRows = 9
    children = Sections.sections.map { section =>
      new ShortButton(section) {
        onAction = _ => {
          sectionField.text = text.value
        }
      }
    }
  }
  private val resetButton = new Button("Reset"){
    onAction = _ => {
      sectionField.clear()
      sectionField.requestFocus()
    }
  }

  showAll()

  sectionField.text.onChange { (_, _, newValue) =>
    if (newValue.isBlank)
      showAll()
    else
      showSome(Sections.find(newValue))
  }

  def showAll(): Unit = children = allSections

  def showSome(sections: Seq[Section]): Unit = {
    children = new VBox() {
      children = sections.map(sectionButtonsMap(_)).appended(resetButton)
    }
  }

  case class LongButton(section: Section) extends Button(section.toString) with SectionButton

  case class ShortButton(section: Section) extends Button(section.code) with SectionButton{
    prefWidth = 55
  }

  trait SectionButton extends ButtonBase {
    val section: Section
    tooltip = section.toString
    onAction = _ => {
      sectionField.text = section.code
    }
  }
}



