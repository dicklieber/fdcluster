
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

package org.wa9nnn.fdcluster.javafx


import org.wa9nnn.fdcluster.javafx.entry.ContestClassValidator
import org.wa9nnn.util.WithDisposition
import scalafx.scene.control.TextField

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class ClassField extends TextField with WithDisposition with NextField {
  setFieldValidator(ContestClassValidator)

  validProperty.onChange{(_,_,nv) =>
    if(nv) {
      // move to next field as soon as class is vallid.
      onDoneFunction("")
    }
  }

}

