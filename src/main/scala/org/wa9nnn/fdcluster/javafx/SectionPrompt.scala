
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.Sections
import scalafx.geometry.Orientation.Vertical
import scalafx.scene.control.{Button, ButtonBase}
import scalafx.scene.layout.{Pane, TilePane, VBox}

class SectionPrompt(implicit sectionField: SectionField) extends Pane {
  private val sectionButtons = Sections.sections.map {
    LongButton
  }
  private val sectionButtonsMap = sectionButtons.map(sb => sb.section -> sb).toMap

  val allSections: TilePane = new TilePane() {
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
      children = sections.map(sectionButtonsMap(_))
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



