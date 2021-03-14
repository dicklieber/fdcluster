
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

import org.wa9nnn.fdcluster.javafx.entry.FieldValidator

object CallsignValidator extends FieldValidator {

  private val callsignRegx =
    """(\p{Upper}{1,2})(\d)(\p{Upper}{1,3})""".r

  def valid(string: String): Option[String] = {
    if (callsignRegx.findFirstIn(string).isDefined)
      None
    else
      Some(errMessage)
  }

  override def errMessage: String = "Not callsign!"
}
