
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

import _root_.scalafx.beans.property.StringProperty
import _root_.scalafx.scene.control.TextInputControl

import scala.util.matching.Regex




object ContestSectionValidator extends FieldValidator {

  def valid(value: String): Option[String] = {
    if (Sections.byCode.contains(value))
      None
    else
      Option(errMessage)
  }

  override def errMessage = "ARRL Section"
}

object AlwaysValid extends FieldValidator {
  override def valid(value: String): Option[String] = None

  override def errMessage = ""

  override def valid(textField: TextInputControl): Option[String] = None
}


trait FieldValidator {
  protected def errMessage: String

  /**
   *
   * @param value to validate
   * @return None if valid otherwise the error message.
   */
  def valid(value: String): Option[String]

  /**
   *
   * @param textInputControl to be validated. [[scalafx.scene.control.TextField]] or [[scalafx.scene.control.TextArea]]
   * @return one if valid otherwise the error message.
   */
  def valid(textInputControl: TextInputControl): Option[String] = {
    valid(textInputControl.getText)
  }
  /**
   *
   * @param stringProperty to be validated.
   * @return one if valid otherwise the error message.
   */
  def valid(stringProperty: StringProperty): Option[String] = {
    valid(stringProperty.value)
  }
}
