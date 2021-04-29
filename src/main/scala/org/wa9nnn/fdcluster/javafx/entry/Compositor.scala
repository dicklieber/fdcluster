
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

package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.beans.property.BooleanProperty

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Listens to a bunch of [[BooleanProperty]]s and updates the state booleanProperty
 *
 * @param boolProperties to be watched.
 */
class Compositor(boolProperties: BooleanProperty*) extends BooleanProperty {
  val bools: Array[AtomicBoolean] = Array.fill(boolProperties.size)(new AtomicBoolean())
  boolProperties.zipWithIndex.foreach { case (bp, i) =>
    bp.onChange { (_, _, nv) =>
      bools(i).set(nv)
      calc()
    }
  }

  def calc(): Unit = {
    value = !bools.exists(_.get()==false)
  }
}
