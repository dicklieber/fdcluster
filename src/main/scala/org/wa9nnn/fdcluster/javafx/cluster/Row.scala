
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

import com.wa9nnn.util.tableui.Cell


trait Row {
  def rowHeader: Cell

  def cells: Seq[Cell]

}

/**
 *
 * @param rowHeader name show in 1st column of row.
 * @param cells     things that can be rendered.
 */
case class MetadataRow(rowHeader: Cell, cells: Seq[Cell]) extends Row
