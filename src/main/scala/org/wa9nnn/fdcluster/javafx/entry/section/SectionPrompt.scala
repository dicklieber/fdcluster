
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx.entry.section

import org.wa9nnn.fdcluster.javafx.entry.Sections
import _root_.scalafx.geometry.Orientation.Vertical
import _root_.scalafx.scene.control.{Button, ButtonBase}
import _root_.scalafx.scene.layout.{Pane, TilePane, VBox}

/**
 * A scalaFx pane that shows sections and allows mouse selection.
 * @param sectionField clicking a buitton will update this textfield.
 */
class   SectionPrompt(implicit sectionField: SectionField) extends Pane {
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



