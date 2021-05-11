
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

package org.wa9nnn.fdcluster.javafx.cluster

import _root_.scalafx.scene.control.{Hyperlink, TableCell}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell

import java.awt.Desktop
import java.net.URI

/**
 * A [[TableCell]] that wotk with [[com.wa9nnn.util.tableui.Cell]]s.
 * These Cells can have css style class, tool tips or href links.
 * @tparam S
 */
class FdClusterTableCell[S] extends TableCell[Row, Cell] with LazyLogging {
  private val desktop = Desktop.getDesktop

  item.onChange { (_, _, cell) =>
    Option(cell).foreach { c: Cell ⇒
      if (c.cssClass.nonEmpty) {
        styleClass = c.cssClass
      }
      if (cell.href.isDefined)
        graphic = new Hyperlink(c.value) {
          onAction = _ => {
            desktop.browse(new URI(cell.href.get.url))
          }
        } else
        text = c.value
      cell.tooltip.foreach {
        tooltip = _
      }

    }

  }
}
