
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

package org.wa9nnn.fdcluster.cabrillo

import com.wa9nnn.cabrillo.model.{SimpleTagValue, TagValue}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{StructuredLogging, Persistence}
import scalafx.beans.property.ObjectProperty

import javax.inject.Inject

/**
 * User provided values.
 * @param preferences
 */
class CabrilloValuesStore @Inject()(preferences: Persistence) extends ObjectProperty[CabrilloExportRequest] with StructuredLogging {
  value = preferences.loadFromFile[CabrilloExportRequest].getOrElse(CabrilloExportRequest())

  onChange { (_, _, newValue) =>
    preferences.saveToFile(newValue)
  }
}

case class CabrilloExportRequest(directory: String = System.getProperty("user.home"), fileName: String = "", cabrilloValues: CabrilloValues = CabrilloValues())

case class CabrilloValue(name: String, value: String) {
  def tagValue: TagValue = SimpleTagValue(name, 0, value)
}

case class CabrilloValues(fieldValues: Seq[CabrilloValue] = Seq.empty) {
  private lazy val map = fieldValues.map(v =>
    v.name -> v.value).toMap

  def valueForName(name: String): Option[String] =
    map.get(name)
}
