
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

import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.TimeFormat.formatLocalDateTime
import scalafx.scene.control.TableCell

class FdClusterTableCell[S, T] extends TableCell[Row, T] with LazyLogging {
  item.onChange { (_, _, newValue) =>
    Option(newValue).foreach { v: T ⇒
      try {
        v match {
          case sa: StyledAny ⇒
            sa.setLabel(this)
          case stamp: LocalDateTime ⇒
            text = stamp
          case string: String ⇒
            text = string
          case other ⇒
            text = other.toString
        }
      } catch {
        case x: Throwable ⇒ x.printStackTrace()
      }
//      val styles = styleClass.toList
//      logger.debug(s"value: $v styles: $styles")
    }

  }
}
